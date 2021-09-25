const { useHandlers } = require('../../util/misc');
const path = require('path');
const Slip = require('../../slip/index');


function handle(rawData, { client }) {
  const { action, data, status } = Slip.deserialize(rawData);

  const handlersDir = path.join(
    __dirname.replace(`${path.sep}event-handlers`, ''),
    'action-handlers'
  );
  useHandlers(null, {
    makeExtraArgs: a => [{ action: a, data, client, status }],
    handlersDir,
    extractOne: action || '',
    defaultHandler: ({ data, status }) => client.displayMessage(data, status),
  });
}

module.exports = handle;