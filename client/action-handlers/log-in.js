const Pillow = require('../pillow');
const { MESSAGES } = require('../util/constants');
const Logger = require('../logger');


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
    const newUsername = await client.getAnswer(MESSAGES.askUsername);
    client.username = newUsername;
    await client.writeSafely({ data: { username: newUsername }, action });
    return;
  }

  if (!data.username) {
    await Logger.log({
      comment: `/=/=/=/=/   USERNAME: ${client.username}   /=/=/=/=/`
    })
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