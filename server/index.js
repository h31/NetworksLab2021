const GenericDnsAccessor = require('../common-classes/generic-dns-accessor');
const { useHandlers } = require('../util/hooks');
const path = require('path');
const InfoLogger = require('../common-classes/info-logger');
const { EVENTS } = require('../util/constants');

async function run({ port, database }) {
  const { existing, sock: server } = await GenericDnsAccessor.createAccessor({
    write: console.log,
    coloured: true
  }, {
    database,
    mark: 'Server'
  });

  if (!existing) {
    // TODO: Fill the DB with data
  }

  useHandlers({
    applyTo: server,
    handlersDir: path.join(__dirname, 'event-handlers'),
    occasionType: InfoLogger.OCCASION_TYPE.event,
    handledEvents: Object.values(EVENTS),
    makeExtraArgs: () => [server]
  });

  server.bind(port);
}

module.exports = run;
