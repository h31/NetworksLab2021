const Message = require('../../common-classes/message');
const InfoLogger = require('../../common-classes/info-logger');

/**
 *
 * @param {Buffer} reqMsg
 * @param {{ port: number, address: string }} rInfo
 * @param {DnsReplicaServer} server
 * @return {Promise<void>}
 */
async function handle(reqMsg, rInfo, server) {
  const parsedReqMessage = Message.parse(reqMsg);
  const { questions, answers } = await server.processRequest(parsedReqMessage);

  const respMsg = Message.makeResponse(parsedReqMessage, questions, answers, [], []);

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