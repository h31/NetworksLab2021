const Logger = require('../logger');
const path = require('path');
const { getParentDir } = require('../util/misc');
const { useHandlers } = require('../util/hooks');

/**
 *
 * @param {Buffer} dataChunk
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function handle(dataChunk, client) {
  const deserializedBody = client.slipHandler.feed(dataChunk);
  if (!deserializedBody) {
    return;
  }
  const { data, status, action } = deserializedBody;
  const handlersDir = path.join(getParentDir(__dirname), 'action-handlers');
  await useHandlers(null, {
    makeExtraArgs: a => [{ action: a, data, client, status }],
    handlersDir,
    extractOne: action || '',
    occasionType: Logger.OCCASION_TYPE.action,
    defaultHandler: ({ data, status }) => client.displayMessage(data, status)
  })();
}

module.exports = handle;