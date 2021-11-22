const Logger = require('../logger');
const { wAmount } = require('../util/misc');

/**
 *
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function handle(client) {
  if (client.writeQueue.length && !client.isWriting) {
    const writeArgs = client.writeQueue.splice(0, 1)[0];

    client.isWriting = true;
    const sentInOneChunk = await client.writeRaw(...writeArgs);
    if (sentInOneChunk) {
      await Logger.log({
        state: Logger.LOG_STATE.written,
        comment: `${wAmount(client.writeQueue.length, 'entry')} waiting to be sent`
      });
      client.isWriting = false;
      client.pingQueue();
    }
  }
}

module.exports = handle;