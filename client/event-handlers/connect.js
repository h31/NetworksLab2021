const Pillow = require('../pillow');

/**
 *
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function handle(client) {
  const toSend = {
    action: Pillow.actions.logIn,
    data: { username: client.username }
  };
  await client.writeSafely(toSend);
}

module.exports = handle;