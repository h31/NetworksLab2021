const Logger = require('../logger');
const { wAmount } = require('../util/misc');

/**
 *
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function handle(client) {
  await Logger.log({
    state: Logger.LOG_STATE.written,
    comment: `${wAmount(client.writeQueue.length, 'entry')} waiting to be sent`
  });
  client.isWriting = false;
  client.pingQueue();
}

module.exports = handle;