const GenericDnsAccessor = require('../common-classes/generic-dns-accessor');
const fs = require('fs');
const path = require('path');
const { useHandlers } = require('../util/hooks');
const InfoLogger = require('../common-classes/info-logger');
const { SOCK_EVENTS, EVENTS, SIGNALS } = require('../util/constants');
const yargs = require('yargs/yargs');
const ResourceRecord = require('../common-classes/resource-record');
const UI = require('./ui');
const TypedError = require('../common-classes/typed-error');

class DnsReplicaClient extends GenericDnsAccessor {
  constructor(...args) {
    super(...args);
  }

  rl;
  yargsParser;
  nextRequestId = 0;
  lastRequest = {
    full: false,
    timeout: null,
    id: null
  };

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
    this.rl.on(SIGNALS.SIGINT, () => UI.displayInfo('The resolver has been closed'));

    useHandlers({
      applyTo: this.sock,
      occasionType: InfoLogger.OCCASION_TYPE.event,
      makeExtraArgs: () => [this],
      handlersDir: path.join(__dirname, 'sock-event-handlers'),
      handledEvents: SOCK_EVENTS
    });
  }

  displayResponse(response) {
    let toDisplay;
    if (this.lastRequest.full) {
      toDisplay = response;
    } else {
      toDisplay = {};
      Object.entries(response).forEach(([key, value]) => {
        if (!['answers', 'authority', 'additional'].includes(key) || !value.length) {
          return;
        }

        toDisplay[key] = value.reduce((res, fullEntry) => ({
          ...res,
          [fullEntry.name]: [...(res[fullEntry.name] || []), fullEntry.data]
        }), {});
      });
    }
    if (Object.keys(toDisplay).length) {
      UI.asList(toDisplay);
    } else {
      UI.displayBright('Nothing found');
    }
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
        address: {
          alias: 'a',
          type: 'string',
          default: '127.0.0.1',
          desc: 'The address of the server you want to use'
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
          choices: Object.keys(ResourceRecord.TYPE_ALIAS),
          type: 'array',
          default: [],
          desc: 'What type of data you want to get (does not have effect for inverse queries). "A" is used for all questions by default'
        },
        class: {
          alias: 'c',
          choices: Object.keys(ResourceRecord.CLASS_ALIAS),
          type: 'array',
          default: [],
          desc: 'What class of data you want to get. "IN" is used for all questions by default'
        },
        wait: {
          alias: 'w',
          type: 'number',
          default: 15,
          desc: 'Maximum time to wait for the response in seconds'
        }
      })
      .check(argv => {
        const availableOptions =
          ['questions', 'full', 'address', 'port', 'recursive', 'type', 'class', 'wait', '_', '$0', 'help', 'version'];
        const unknownOptions = Object.keys(argv).filter(providedOpt => !availableOptions.includes(providedOpt));
        if (unknownOptions.length) {
          throw new TypedError(`Unknown options: ${unknownOptions.join(',')}`, TypedError.TYPE.validation);
        }

        return true;
      })
      .parserConfiguration({ 'strip-aliased': true });

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