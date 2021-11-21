/**
 *
 * @param {Object} data
 * @param {TossClient} client
 * @param {number} status
 * @return {Promise<void>}
 */
async function handle({ data, client, status }) {
  client.displayMessage({ time: data.time, message: `${data.username} left the chat` }, status);
}

module.exports = handle;