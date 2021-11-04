const InfoLogger = require('../../common-classes/info-logger');

/**
 *
 * @param {DnsReplicaServer} server
 * @return {Promise<void>}
 */
async function handle(server) {
  const address = server.sock.address();
  await InfoLogger.log({
    status: InfoLogger.STATUS.info,
    comment: `Server is listening on ${address.address}:${address.port}`
  });
}

module.exports = handle;