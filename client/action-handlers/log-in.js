const Pillow = require('../pillow');
const { MESSAGES } = require('../util/constants');


/**
 *
 * @param {Object} data
 * @param {TossClient} client
 * @param {number} status
 * @param {string} action
 * @return {Promise<void>}
 */
async function handle({ data, client, status, action }) {
  if (Pillow.isError(status)) {
    client.displayMessage(data, status);
    const newUsername = await new Promise(resolve => client.rl.question(
      MESSAGES.askUsername, answer => resolve(answer)
    ));
    client.username = newUsername;
    await client.writeSafely({ username: newUsername, action });
    return;
  }

  client.displayMessage(
    {
      time: data.time,
      message: data.username
        ? `${data.username} entered the chat`
        : `Welcome to Toss-a-Message, ${client.username}!`
    },
    status
  );
  client.startRlLoop();
}

module.exports = handle;