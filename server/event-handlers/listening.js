const InfoLogger = require('../../common-classes/info-logger');

async function handle(server) {
  const address = server.address();
  await InfoLogger.log({
    status: InfoLogger.STATUS.info,
    comment: `Server is listening on ${address.address}:${address.port}`
  });
}

module.exports = handle;