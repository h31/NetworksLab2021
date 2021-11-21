const SlipError = require('../slip/error');
const { MESSAGES, ERRORS } = require('../util/constants');
const UI = require('../ui');
const Logger = require('../logger');

async function handle(error) {
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

  UI.alertError(text);
  const comment = error.code || error.message;
  await Logger.log({
    occasionType: Logger.OCCASION_TYPE.error,
    occasionName: error.constructor.name,
    comment
  });
}

module.exports = handle;