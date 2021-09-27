const { useHandlers, handleChunks } = require('../../util/misc');
const path = require('path');
const Slip = require('../../slip/index');
const log = require('../client-logger');
const { LOG_TYPES } = require('../../util/constants');


function handle(rawData, { client }) {
  const { shouldContinueHandling, dataToHandle } = handleChunks(client, rawData);
  if (!shouldContinueHandling) {
    return;
  }
  const { action, data, status } = Slip.deserialize(dataToHandle);

  const handlersDir = path.join(
    __dirname.replace(`${path.sep}event-handlers`, ''),
    'action-handlers'
  );
  useHandlers(null, {
    makeExtraArgs: a => [{ action: a, data, client, status }],
    handlersDir,
    extractOne: action || '',
    defaultHandler: ({ data, status }) => client.displayMessage(data, status),
    log: async (a, handler) => await log(handler ? 'handled' : 'skipped', a, LOG_TYPES.Action, client.logSuffix)
  });
}

module.exports = handle;