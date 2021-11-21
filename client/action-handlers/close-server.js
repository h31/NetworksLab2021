/**
 *
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function handle({ client }) {
  client.isServerActive = false;
}

module.exports = handle;
