const GenericDnsAccessor = require('../common-classes/generic-dns-accessor');
const fs = require('fs');
const path = require('path');
const { useHandlers } = require('../util/hooks');
const InfoLogger = require('../common-classes/info-logger');
const { SOCK_EVENTS, EVENTS } = require('../util/constants');
const yargs = require('yargs/yargs');
const ResourceRecord = require('../common-classes/resource-record');

class DnsReplicaClient extends GenericDnsAccessor {
  constructor(...args) {
    super(...args);
  }

  rl;
  yargsParser;
  nextRequestId = 1;

  /**
   *
   * @param {number} database
   * @return {Promise<{existing: boolean, accessor: DnsReplicaClient}>}
   */
  static async createAccessor(database) {
    try {
      await fs.promises.mkdir('client-logs');
    } catch {}
    const uniqueFileSuffix = (new Date()).getTime();
    const logDir = path.join(
      __dirname.replace(`${path.sep}client`, ''),
      'client-logs',
      `${uniqueFileSuffix}.txt`
    );
    const write = async text => fs.promises.appendFile(logDir, `${text}\n`);
    return super.createAccessor({
      write, coloured: false
    }, {
      database, mark: 'Client'
    });
  }

  runClient() {
    this.rl = this.runRl();

    useHandlers({
      applyTo: this.sock,
      occasionType: InfoLogger.OCCASION_TYPE.event,
      makeExtraArgs: () => [this],
      handlersDir: path.join(__dirname, 'sock-event-handlers'),
      handledEvents: SOCK_EVENTS
    });
  }

  runInteractionLoop() {
    this.yargsParser = yargs()
      .options({
        questions: {
          alias: 'q',
          type: 'array',
          demandOption: true,
          desc: 'The domain names / IP addresses you want to ask the server about'
        },
        full: {
          alias: 'f',
          type: 'boolean',
          default: false,
          desc: 'Display full response'
        },
        host: {
          alias: 'h',
          type: 'string',
          default: '127.0.0.1',
          desc: 'The host of the server you want to use'
        },
        port: {
          alias: 'p',
          type: 'number',
          default: 41234,
          desc: 'The port of the server you want to use'
        },
        recursive: {
          alias: 'r',
          type: 'boolean',
          default: true,
          desc: 'Desire recursion when processing the request'
        },
        type: {
          alias: 't',
          type: 'array',
          default: [],
          desc: 'What type of data you want to get (does not have effect for inverse queries). "ipv4" is used for all questions by default'
        },
        class: {
          alias: 'c',
          type: 'array',
          default: [],
          desc: 'What class of data you want to get. "internet" is used for all questions by default'
        }
      });

    this.rl.prompt();
    useHandlers({
      applyTo: this.rl,
      occasionType: InfoLogger.OCCASION_TYPE.event,
      makeExtraArgs: () => [this],
      handlersDir: path.join(__dirname, 'rl-event-handlers'),
      handledEvents: [EVENTS.line]
    });
  }
}

module.exports = DnsReplicaClient;