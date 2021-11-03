const InfoLogger = require('../../common-classes/info-logger');

async function handle() {
  await InfoLogger.log({
    status: InfoLogger.STATUS.info,
    comment: `Closed the server`
  });
}

module.exports = handle;