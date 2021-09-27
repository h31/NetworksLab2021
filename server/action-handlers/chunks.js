const Pillow = require('../../pillow/index');
const Slip = require('../../slip/index');

function handle({ data: { chunks }, client, action }) {
  client.chunksToReceive = chunks;
  client.writeSafe([Slip.serialize({ action, status: Pillow.responseStatus.OK_EMPTY.code })]);
}

module.exports = handle;