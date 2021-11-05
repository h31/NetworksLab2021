const Message = require('../../common-classes/message');
const InfoLogger = require('../../common-classes/info-logger');
const UI = require('../ui');
const { getQuestionsDescription, getAField } = require('../../util/dns');
const { OPCODE } = require('../../util/constants');
const ResourceRecord = require('../../common-classes/resource-record');

/**
 *
 * @param {string} input
 * @param {DnsReplicaClient} client
 * @return {Promise<void>}
 */
async function handle(input, client) {
  const [parsingErr, argv, output] = await new Promise(resolve =>
    client.yargsParser.parse(input, (...res) => resolve([...res]))
  );
  if (!parsingErr) {
    const { opCode, typedQuestions } = getQuestionsDescription(argv.questions);

    const pseudoReq = { opCode };
    if (opCode === OPCODE.inverseQuery) {
      pseudoReq.questions = [];
      pseudoReq.answers = typedQuestions.map((q, idx) => ({
        name: '',
        type: q.type,
        class: ResourceRecord.CLASS[argv.class[idx]] || ResourceRecord.CLASS.internet,
        data: { [getAField(q.type)]: q.question }
      }));
    } else {
      pseudoReq.questions = typedQuestions.map((q, idx) => ({
        name: q.question,
        type: ResourceRecord.TYPE[argv.type[idx]] || ResourceRecord.TYPE.ipv4,
        class: ResourceRecord.CLASS[argv.class[idx]] || ResourceRecord.CLASS.internet
      }));
      pseudoReq.answers = [];
    }

    const { questions, answers, noDataFor } = await client.processRequest(pseudoReq);

    await InfoLogger.log({
      comment: `From cache: ${typedQuestions.length - noDataFor.length}. Requesting server for: ${noDataFor.length}`
    });

    if (opCode === OPCODE.inverseQuery && questions.length || opCode === OPCODE.standardQuery && answers.length) {
      UI.asList({
        cached: true,
        questions,
        answers
      });
    }

    if (!noDataFor.length) {
      client.rl.prompt();
      return;
    } else {
      UI.displayInfo('Please wait a bit, asking the server for data not found in cache...');
    }

    const message = Message.makeRequest(
      client.nextRequestId++, noDataFor,
      { opCode, recDesired: argv.recursive }
    );

    const [sendingErr, bytes] = await new Promise(resolve =>
      client.sock.send(message, 0, message.byteLength, argv.port, argv.host, (...res) => resolve([...res]))
    );
    await InfoLogger.log({
      comment: `Sent ${bytes} bytes of data to ${argv.host}:${argv.port}`
    });
    client.rl.pause();
  } else {
    UI.displayError(output);
    client.rl.prompt();
  }
}

module.exports = handle;
