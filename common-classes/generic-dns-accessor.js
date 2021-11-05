const InfoLogger = require('./info-logger');
const { SIGNALS, EVENTS, OPCODE } = require('../util/constants');
const dgram = require('dgram');
const ConvenientRedis = require('./convenient-redis');
const { setupRl } = require('../util/rl');
const { getAField } = require('../util/dns');

class GenericDnsAccessor {
  sock;
  convenientRedis;

  /**
   *
   * @param {Object} loggerConfig configurations for the InfoLogger
   * @param {function(string): void | Promise<void>} loggerConfig.write how to output the built message
   * @param {boolean} [loggerConfig.coloured] should colours be used
   * @param {Object} dbConfig configurations for the Redis database
   * @param {number=} dbConfig.database database number (if none provided, a new one will be created with automatic number)
   * @param {string} dbConfig.mark what the database would be used for ('server', 'client' etc.)
   */
  constructor({ write, coloured }, { database, mark }) {
    InfoLogger.init(write, coloured);

    this.sock = dgram.createSocket('udp4');
    this.convenientRedis = new ConvenientRedis(database, mark);
  }

  /**
   *
   * @return {Promise<{existing: boolean, accessor: GenericDnsAccessor}>}
   */
  static async createAccessor(...args) {
    const accessor = new this(...args);
    const existing = await accessor.convenientRedis.init();

    process.on(SIGNALS.SIGINT, () => accessor.closeEverything());
    accessor.convenientRedis.on(EVENTS.error, err => accessor.closeEverything(err));
    accessor.sock.on(EVENTS.error, err => accessor.closeEverything(err));

    return { existing, accessor };
  }

  async closeEverything(err) {
    if (err) {
      await InfoLogger.log({
        status: InfoLogger.STATUS.error,
        occasionType: InfoLogger.OCCASION_TYPE.error,
        occasionName: err.constructor.name,
        comment: err.message
      });
    }

    await this.convenientRedis.endSession();
    this.sock.close();

    process.exit(err ? 1 : 0);
  }

  runRl() {
    const rl = setupRl();
    rl.on(EVENTS.close, () => this.closeEverything());
    return rl;
  }

  async processRequest(req) {
    const questions = [...req.questions];
    const answers = [];
    let noDataFor;
    if (req.opCode === OPCODE.inverseQuery) {
      answers.push(...req.answers);
      await this.convenientRedis.scanRecords(dbRecord => {
        for (const idx in answers) {
          const fakeAnswer = answers[idx];
          if (fakeAnswer.type === +dbRecord.type && fakeAnswer.class === +dbRecord.class) {
            const field = getAField(fakeAnswer.type);
            if (fakeAnswer.data[field] === dbRecord.data[field]) {
              answers[idx].name = dbRecord.name;
              answers[idx].ttl = +dbRecord.ttl;
              questions[idx] = {
                name: dbRecord.name,
                type: +dbRecord.type,
                class: +dbRecord.class
              };
              return false;
            }
          }
        }

        return !answers.some(ans => !ans.name);
      });
      noDataFor = answers.reduce((res, ans) =>
        ans.name ? res : [...res, ans]
      );
    } else {
      noDataFor = [];
      for (const question of req.questions) {
        const possibleAnswers = await this.convenientRedis.getAllByTag(question.name);
        const filteredAnswers = possibleAnswers.filter(ans => +ans.type === question.type && +ans.class === question.class);
        if (filteredAnswers.length) {
          answers.push(...filteredAnswers);
        } else {
          noDataFor.push(question);
        }
      }
    }

    return { questions, answers, noDataFor };
  }
}

module.exports = GenericDnsAccessor;