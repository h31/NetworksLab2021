const { ERRORS, MESSAGES } = require('../../util/constants');
const SlipError = require('../../slip/error');
const TossMessenger = require('../toss-messenger');


function handle(error) {
  let text;
  if (error instanceof SlipError) {
    text = MESSAGES.strangeServerResponse;
  } else {
    switch (error.code) {
      case ERRORS.ECONNREFUSED:
        const { port, address } = error;
        text = `Could not connect to server at ${address}:${port}`;
        break;
      case ERRORS.ECONNRESET:
        text = MESSAGES.serverError;
        break;
      default:
        text = MESSAGES.unknownError;
    }
  }

  TossMessenger.alertError(text);
}

module.exports = handle;