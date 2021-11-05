const Message = require('../../common-classes/message');
const InfoLogger = require('../../common-classes/info-logger');
const ResourceRecord = require('../../common-classes/resource-record');

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
  const [sendingErr, bytes] = await new Promise(resolve =>
    server.sock.send(
      respMsg, 0, respMsg.byteLength, rInfo.port, rInfo.address,
      (...res) => resolve([...res])
    )
  );
  await InfoLogger.log({
    comment: `Sent ${bytes} bytes of data to ${rInfo.address}:${rInfo.port}`,
    status: InfoLogger.STATUS.success
  });
}

module.exports = handle;