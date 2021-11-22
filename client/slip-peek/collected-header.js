const Logger = require('../logger');
const { wAmount } = require('../util/misc');

async function handle(sz) {
  await Logger.log({
    comment: `Collected the full Header, expecting ${wAmount(sz, 'byte')} of Body`
  });
}

module.exports = handle;
