const UI = require('../ui');
const Message = require('../../common-classes/message');

/**
 *
 * @param {Buffer} respMsg
 * @param {{ port: number, address: string }} rInfo
 * @param {DnsReplicaClient} client
 * @return {Promise<void>}
 */
function handle(respMsg, rInfo, client) {
  UI.asList(Message.parse(respMsg));
  client.rl.prompt();
}

module.exports = handle;