const UI = require('../ui');
const Logger = require('../logger');

/**
 *
 * @param {boolean} hadError
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function handle(hadError, client) {
  if (!hadError) {
    if (!client.isServerActive) {
      UI.alertError('The Toss Server has been closed');
    } else {
      UI.alertChatBot(`Goodbye${client.username ? `, ${client.username}` : ''}!`)
    }
  } else {
    UI.alertError('Woops! Closed due to an error');
  }

  await Logger.log({
    comment: hadError ? 'The Toss Client was closed due to an error' : 'The Toss Client was gracefully closed'
  });
}

module.exports = handle;