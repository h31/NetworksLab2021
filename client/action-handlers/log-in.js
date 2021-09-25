const Pillow = require('../../pillow/index');
const { MESSAGES } = require('../../util/constants');


function handle({ data, client, status, action }) {
  client.displayMessage(
    {
      time: data.time,
      message: data.username
        ? `${data.username} entered the chat`
        : `Welcome to Toss-a-Message, ${client.username}!`
    },
    status
  );
  if (Pillow.isError(status)) {
    client.rl.question(MESSAGES.askUsername, answer => {
      client.username = answer;
      client.req(action, { username: answer });
    });
    return;
  }
  client.startRlLoop();
}

module.exports = handle;