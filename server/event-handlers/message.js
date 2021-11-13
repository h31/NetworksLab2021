const Message = require('../../common-classes/message');
const InfoLogger = require('../../common-classes/info-logger');
const TypedError = require('../../common-classes/typed-error');
const { RESP_CODE, OPCODE } = require('../../util/constants');

/**
 *
 * @param {Buffer} reqMsg
 * @param {{ port: number, address: string }} rInfo
 * @param {DnsReplicaServer} server
 * @return {Promise<void>}
 */
async function handle(reqMsg, rInfo, server) {
  const parsedReqMessage = Message.parse(reqMsg);

  const { error, id } = parsedReqMessage;
  if (id == null && error != null) {
    // can't respond at all without knowing at least the id of the request
    if (error.type === TypedError.TYPE.validation) {
      return; // the problem is not server-side
    } else {
      throw error;
    }
  }

  if (parsedReqMessage.opCode == null) {
    parsedReqMessage.opCode = OPCODE.status;
  }
  if (parsedReqMessage.recDes == null) {
    parsedReqMessage.recDes = 0;
  }

  let respCode;
  let questions = [];
  let answers = [];

  if (error == null) {
    const processed = await server.processRequest(parsedReqMessage);
    questions = processed.questions;
    answers = processed.answers;
    respCode = processed.noDataFor.length ? RESP_CODE.noSuchDomainName : RESP_CODE.noError;
  } else {
    await InfoLogger.log({
      status: InfoLogger.STATUS.error,
      comment: `(Occurred while parsing the request) ${error.message}`,
      occasionType: InfoLogger.OCCASION_TYPE.error,
      occasionName: error.type || error.constructor.name
    });
    respCode = error instanceof TypedError ? RESP_CODE.formatError : RESP_CODE.serverError;
  }

  const respMsg = Message.makeResponse(parsedReqMessage, respCode, questions, answers, [], []);

  const bytes = await new Promise((resolve, reject) =>
    server.sock.send(
      respMsg, 0, respMsg.byteLength, rInfo.port, rInfo.address,
      (error, bytes) => error ? reject(error) : resolve(bytes)
    )
  );
  await InfoLogger.log({
    comment: `Sent ${bytes} bytes of data to ${rInfo.address}:${rInfo.port}`,
    status: InfoLogger.STATUS.success
  });
}

module.exports = handle;