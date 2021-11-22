const Logger = require('../logger');

async function handle() {
  await Logger.log({ comment: `Collected the full Body` });
}

module.exports = handle;
