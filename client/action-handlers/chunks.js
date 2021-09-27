const Pillow = require('../../pillow/index');
const TossMessenger = require('../toss-messenger');
const { EVENTS } = require('../../util/constants');

function handle({ data, status, client }) {
  if (Pillow.isError(status)) {
    TossMessenger.write(data, status);
    return;
  }

  if (status === Pillow.responseStatus.OK.code) {
    client.chunksToReceive = data.chunks;
  } else {
    client.emit(EVENTS.proceedQueue);
  }
}

module.exports = handle;