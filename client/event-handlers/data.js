const { useHandlers, handleChunks } = require('../../util/misc');
const path = require('path');
const Slip = require('../../slip/index');
const { LOG_TYPES, LOG_STATES } = require('../../util/constants');


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
    log: (a, handler) => client.logger({
      type: LOG_TYPES.Action,
      name: a,
      state: handler ? LOG_STATES.passedToHandle : LOG_STATES.skipped,
    })
  });
}

module.exports = handle;