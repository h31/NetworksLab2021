const Message = require('../../common-classes/message');
const InfoLogger = require('../../common-classes/info-logger');

/**
 *
 * @param {Buffer} respMsg
 * @param {{ port: number, address: string }} rInfo
 * @param {DnsReplicaClient} client
 * @return {Promise<void>}
 */
async function handle(respMsg, rInfo, client) {
  const parsedResponse = Message.parse(respMsg);
  if (parsedResponse.id !== client.lastRequest.id) {
    await InfoLogger.log({
      comment: `Received a timed out response for id = ${parsedResponse.id}`
    });
    return;
  }

  clearTimeout(client.lastRequest.timeout);

  for (const answer of parsedResponse.answers) {
    if (answer.ttl) {
      await client.convenientRedis.insertWithTag(answer, answer.name, answer.ttl);
    }
  }
  const toDisplay = Message.parse(respMsg);
  client.displayResponse(toDisplay);
  client.rl.prompt();
}

module.exports = handle;