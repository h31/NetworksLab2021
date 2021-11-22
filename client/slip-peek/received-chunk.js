const Logger = require('../logger');
const { wAmount } = require('../util/misc');

async function handle(sz, part) {
  await Logger.log({
    comment: `Received ${wAmount(sz, 'byte')} of data (collecting ${part})`
  });
}

module.exports = handle;
