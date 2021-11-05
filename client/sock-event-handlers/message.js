const UI = require('../ui');
const Message = require('../../common-classes/message');

/**
 *
 * @param {Buffer} respMsg
 * @param {{ port: number, address: string }} rInfo
 * @param {DnsReplicaClient} client
 * @return {Promise<void>}
 */
async function handle(respMsg, rInfo, client) {
  const parsedResponse = Message.parse(respMsg);
  for (const answer of parsedResponse.answers) {
    if (answer.ttl) {
      await client.convenientRedis.insertWithTag(answer, answer.name, answer.ttl);
    }
  }
  UI.asList(Message.parse(respMsg));
  client.rl.prompt();
}

module.exports = handle;