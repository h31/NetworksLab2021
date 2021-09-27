const { useHandlers, handleChunks } = require('../../util/misc');
const path = require('path');
const Slip = require('../../slip/index');
const log = require('../client-logger');
const { LOG_TYPES, LOG_NAMES } = require('../../util/constants');


function handle(rawData, { client }) {
  const { shouldContinueHandling, dataToHandle } = handleChunks(
    client, rawData,
    async (receivedChunks, chunksToReceive) => await log(
      `Received ${receivedChunks}, ${chunksToReceive ? `${chunksToReceive} chunks to go` : 'no more to receive'}`,
      LOG_NAMES.chunksReceived, LOG_TYPES.Chunks, client.logSuffix
    )
  );
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