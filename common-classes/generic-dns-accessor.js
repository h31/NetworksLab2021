const InfoLogger = require('./info-logger');
const { SIGNALS, EVENTS, OPCODE } = require('../util/constants');
const dgram = require('dgram');
const ConvenientRedis = require('./convenient-redis');
const { getAField } = require('../util/dns');
const TypedError = require('./typed-error');
const ConvenientRl = require('./convenient-rl');

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

  ensureFinalization() {
    process.on(SIGNALS.SIGINT, () => this.closeEverything());
    this.convenientRedis.on(EVENTS.error, err => this.closeEverything(err));
    this.sock.on(EVENTS.error, err => this.closeEverything(err));
  }

  /**
   *
   * @return {Promise<{existing: boolean, accessor: GenericDnsAccessor}>}
   */
  static async createAccessor(...args) {
    const accessor = new this(...args);
    const existing = await accessor.convenientRedis.init();

    accessor.ensureFinalization();

    return { existing, accessor };
  }

  async closeEverything(err) {
    if (err) {
      await InfoLogger.log({
        status: InfoLogger.STATUS.error,
        occasionType: InfoLogger.OCCASION_TYPE.error,
        occasionName: err instanceof TypedError ? err.type : err.constructor.name,
        comment: err.message
      });
    }

    await this.convenientRedis.endSession();
    this.sock.close();

    process.exit(err ? 1 : 0);
  }

  runRl() {
    const rl = new ConvenientRl();
    rl.on(SIGNALS.SIGINT, () => this.closeEverything());
    return rl;
  }

  async processRequest(req) {
    const questions = [...req.questions];
    const answers = [];
    let noDataFor;
    if (req.opCode === OPCODE.inverseQuery) {
      answers.push(...req.answers);
      await this.convenientRedis.scanRecords(dbRecord => {
        if (!answers.some(ans => !ans.name)) {
          return true;
        }

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
              break;
            }
          }
        }

        return false;
      });
      noDataFor = answers.filter(ans => !ans.name);
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