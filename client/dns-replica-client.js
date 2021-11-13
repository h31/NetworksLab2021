const GenericDnsAccessor = require('../common-classes/generic-dns-accessor');
const fs = require('fs');
const path = require('path');
const { useHandlers } = require('../util/hooks');
const InfoLogger = require('../common-classes/info-logger');
const { SOCK_EVENTS, EVENTS, SIGNALS, RESP_CODE, OPCODE } = require('../util/constants');
const yargs = require('yargs/yargs');
const ResourceRecord = require('../common-classes/resource-record');
const UI = require('./ui');
const TypedError = require('../common-classes/typed-error');
const { startCase, validateIpV4 } = require('../util/misc');
const Message = require('../common-classes/message');
const { getAField } = require('../util/dns');

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

  static #TITLE_MAPPING = {
    qr: 'QR',
    opCode: 'Operation code',
    authAns: 'Authoritative answer',
    trunc: 'Truncated',
    recDes: 'Recursion desired',
    recAv: 'Recursion available',
    rCode: 'Response code',
    qdCount: 'Questions amount',
    anCount: 'Answers amount',
    nsCount: 'Authority records amount',
    arCount: 'Additional records amount',
    questions: 'Questions',
    answers: 'Answers',
    authority: 'Authority',
    additional: 'Additional',

    ttl: 'TTL',
    rdLength: 'Data section size',
    class: 'Class',
    type: 'Type',
    data: 'Data',
    cached: 'Pulled from cache',
    name: 'Domain name',

    a: 'ipv4 address',
    aaaa: 'ipv6 address',

    mName: 'Responsible domain name',
    rName: 'Mailbox for the zone',
    serial: 'Serial number',
    refresh: 'Refresh interval',
    retry: 'Retry timeout',
    expire: 'Expires in',
    minimum: 'Minimum TTL',

    preference: 'Preference level',
    exchange: 'Mail exchange host',

    text: 'Text'
  };

  static #getDisplayedValue(originalKey, originalValue) {
    if (originalKey === 'rdLength') {
      return `${originalValue} bytes`;
    }

    if (['ttl', 'minimum', 'retry', 'expire', 'refresh'].includes(originalKey)) {
      return `${originalValue} sec.`;
    }

    if (['rCode', 'opCode', 'type', 'class', 'qr'].includes(originalKey)) {
      const mapping = {
        rCode: RESP_CODE,
        opCode: OPCODE,
        type: ResourceRecord.TYPE,
        class: ResourceRecord.CLASS,
        qr: Message.QR
      }[originalKey];
      const comment = startCase(Object.entries(mapping).find(([, val]) => val === +originalValue)[0]);
      return `${originalValue} (${comment})`;
    }

    if (['recAv', 'recDes', 'trunc', 'authAns'].includes(originalKey)) {
      return `${Boolean(originalValue)}`;
    }

    return originalValue;
  }

  displayResponse(response) {
    let toDisplay;
    if (this.lastRequest.full) {
      toDisplay = response;
    } else {
      toDisplay = {};
      Object.entries(response).forEach(([key, value]) => {
        const isImportantData = ['authority', 'additional', 'answers'].includes(key);
        if (!isImportantData) {
          return;
        }

        const meaningfulLength = (key === 'answers' && response.opCode === OPCODE.inverseQuery
          ? value.filter(fakeAns => !!fakeAns.name)
          : value).length;
        if (!meaningfulLength) {
          return;
        }

        toDisplay[key] = value.reduce((res, fullEntry) => ({
          ...res,
          [fullEntry.name]: [...(res[fullEntry.name] || []), fullEntry.data]
        }), {});
      });
    }

    if (Object.keys(toDisplay).length) {
      UI.asList(toDisplay, {
        titleMapping: DnsReplicaClient.#TITLE_MAPPING,
        getValue: DnsReplicaClient.#getDisplayedValue
      });
    }

    if (response.cached) {
      return;
    }

    let notFoundText;
    let notFoundKind;
    if (response.opCode === OPCODE.inverseQuery) {
      notFoundText = response.answers
        .filter(fakeAns => !fakeAns.name)
        .map(fakeAns => fakeAns.data[getAField(fakeAns.type)])
        .join(', ');
      notFoundKind = 'domain name';
    } else {
      notFoundText = response.questions
        .filter(q => !response.answers.some(ans => ans.name === q.name))
        .map(q => q.name)
        .join(', ');
      notFoundKind = 'data of requested type';
    }

    if (notFoundText) {
      UI.displayBright(
        response.rCode === RESP_CODE.noRelatedData
          ? `No ${notFoundKind} registered for ${notFoundText}`
          : `No ${notFoundKind} found for ${notFoundText} at specified name server. Try using some other server`
      );
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
          desc: 'Desire recursion when processing the request (use --no-r to deny default recursive behaviour)'
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

        if (!validateIpV4(argv.address)) {
          throw new TypedError(`Invalid address: ${argv.address}`, TypedError.TYPE.validation);
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