const GenericDnsAccessor = require('../common-classes/generic-dns-accessor');
const fs = require('fs');
const path = require('path');
const UI = require('./ui');
const { useHandlers } = require('../util/hooks');
const InfoLogger = require('../common-classes/info-logger');
const { EVENTS } = require('../util/constants');

async function run({ database }) {
  try {
    await fs.promises.mkdir('client-logs');
  } catch {}
  const uniqueFileSuffix = (new Date()).getTime();
  const logDir = path.join(
    __dirname.replace(`${path.sep}client`, ''),
    'client-logs',
    `${uniqueFileSuffix}.txt`
  );
  const write = async text => fs.promises.appendFile(logDir, `${text}\n`);

  const { existing, sock: client, dbClient } = await GenericDnsAccessor.createAccessor({
    write, coloured: false
  }, {
    database, mark: 'Client'
  });
  UI.initTheme();

  useHandlers({
    applyTo: client,
    occasionType: InfoLogger.OCCASION_TYPE.event,
    makeExtraArgs: () => [client],
    handlersDir: path.join(__dirname, 'event-handlers'),
    handledEvents: [EVENTS.close, EVENTS.message]
  });
}

module.exports = run;