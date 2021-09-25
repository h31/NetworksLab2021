const initRl = require('./rl');
const TossClient = require('./toss-client');
const path = require('path');
const TossMessenger = require('./toss-messenger');
const { MESSAGES } = require('../util/constants');

function run({ port, host }) {
  const rl = initRl();
  TossMessenger.initClientTheme();
  const client = new TossClient({ handlersDir: path.join(__dirname, 'event-handlers') }, rl);

  rl.question(MESSAGES.askUsername, answer => {
    client.username = answer;
    client.connect(port, host);
  });
}

module.exports = run;