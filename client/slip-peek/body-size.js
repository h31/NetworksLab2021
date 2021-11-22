const Logger = require('../logger');
const { wAmount } = require('../util/misc');

async function handle(sz) {
  await Logger.log({
    comment: `Sending ${wAmount(sz, 'byte')} of Body`
  });
}

module.exports = handle;
