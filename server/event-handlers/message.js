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
  const allAnswers = [];
  for (const question of parsedReqMessage.questions) {
    const answersForQuestion = await server.convenientRedis.getAllByTag(question.name);
    allAnswers.push(...answersForQuestion.filter(ans => +ans.type === question.qType && +ans.class === question.qClass));
  }

  const respMsg = Message.makeResponse(parsedReqMessage, allAnswers, [], []);
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