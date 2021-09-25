const TossServer = require('./toss-server');
const path = require('path');
const TossLogger = require('./toss-logger');


function run({ port, host }) {
  TossLogger.initServerTheme();
  const server = new TossServer({ handlersDir: path.join(__dirname, 'event-handlers'), clientIndicator: 'username' });
  server.listen(port, host);
}

module.exports = run;