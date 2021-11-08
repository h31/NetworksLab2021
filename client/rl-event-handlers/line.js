const Message = require('../../common-classes/message');
const InfoLogger = require('../../common-classes/info-logger');
const UI = require('../ui');
const { getQuestionsDescription, getAField } = require('../../util/dns');
const { OPCODE } = require('../../util/constants');
const ResourceRecord = require('../../common-classes/resource-record');
const TypedError = require('../../common-classes/typed-error');

/**
 *
 * @param {string} input
 * @param {DnsReplicaClient} client
 * @return {Promise<void>}
 */
async function handle(input, client) {
  try {
    client.rl.interactive = false;

    const parseResult = await new Promise((resolve, reject) =>
      client.yargsParser.parse(input, (err, argv, output) => {
          if (output) {
            const toDisplay = err ? output.replace(err.message, '') : output;
            UI.displayPlain(toDisplay);
          }
          return err
            ? reject(new TypedError(err.message, TypedError.TYPE.validation))
            : resolve(output ? null : argv);
        }
      )
    );

    if (!parseResult) {
      client.rl.prompt();
      return;
    }

    const { opCode, typedQuestions } = getQuestionsDescription(parseResult.questions);

    const getProp = (idx, prop, defaultV) => {
      const alias = parseResult[prop][idx];
      const upperProp = prop.toUpperCase();
      const typeName = ResourceRecord[`${upperProp}_ALIAS`][alias];
      return ResourceRecord[upperProp][typeName] || ResourceRecord[upperProp][defaultV];
    };
    const getType = idx => getProp(idx, 'type', 'ipv4');
    const getClass = idx => getProp(idx, 'class', 'internet');

    const pseudoReq = { opCode };
    if (opCode === OPCODE.inverseQuery) {
      pseudoReq.questions = [];
      pseudoReq.answers = typedQuestions.map((q, idx) => ({
        name: '',
        type: q.type,
        class: getClass(idx),
        data: { [getAField(q.type)]: q.question }
      }));
    } else {
      pseudoReq.questions = typedQuestions.map((q, idx) => ({
        name: q.question,
        type: getType(idx),
        class: getClass(idx)
      }));
      pseudoReq.answers = [];
    }

    const { questions, answers, noDataFor } = await client.processRequest(pseudoReq);
    client.lastRequest.full = parseResult.full;

    await InfoLogger.log({
      comment: `From cache: ${typedQuestions.length - noDataFor.length}. Requesting server for: ${noDataFor.length}`
    });

    if (opCode === OPCODE.inverseQuery && questions.length || opCode === OPCODE.standardQuery && answers.length) {
      client.displayResponse({
        cached: true,
        questions,
        answers
      });
    }

    if (!noDataFor.length) {
      client.rl.prompt();
      return;
    }

    const message = Message.makeRequest(
      ++client.nextRequestId, noDataFor,
      { opCode, recDesired: parseResult.recursive }
    );

    UI.displayInfo('Please wait a bit, asking the server for data not found in cache...');

    const bytes = await new Promise((resolve, reject) =>
      client.sock.send(
        message, 0, message.byteLength, parseResult.port, parseResult.address,
        (error, bytes) => {
          if (error) {
            reject(error);
          } else {
            client.lastRequest.id = client.nextRequestId;
            client.lastRequest.timeout = setTimeout(() => {
              UI.displayError('The request has timed out');
              client.lastRequest.id = null;
              client.rl.prompt();
            }, parseResult.wait * 1000);
            resolve(bytes);
          }
        })
    );
    await InfoLogger.log({
      comment: `Sent ${bytes} bytes of data to ${parseResult.address}:${parseResult.port}`
    });
  } catch (e) {
    if (!(e instanceof TypedError)) {
      throw e;
    }

    await InfoLogger.log({
      comment: e.message,
      occasionType: InfoLogger.OCCASION_TYPE.error,
      occasionName: e.type
    });

    UI.displayError(e.message);
    client.rl.prompt();
  }
}

module.exports = handle;
