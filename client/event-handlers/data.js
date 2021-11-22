const { MESSAGE_PART } = require('../util/constants');
const Logger = require('../logger');
const Slip = require('../slip');
const path = require('path');
const { getParentDir, wAmount } = require('../util/misc');
const { useHandlers } = require('../util/hooks');

/**
 *
 * @param {Buffer} dataChunk
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function collectHeader(dataChunk, client) {
  const size = dataChunk.byteLength;
  let idx = 0;

  while (idx < size) {
    const oneByte = dataChunk[idx++];
    const meaningfulPart = oneByte % 128;
    client.toCollect += meaningfulPart * (2 ** client.headerChunkIdx);
    client.headerChunkIdx += 7;

    if (oneByte < 128) {
      client.currentMessagePart = MESSAGE_PART.BODY;
      client.body = Buffer.alloc(0);
      await Logger.log({
        comment: `Collected the full Header, expecting ${wAmount(client.toCollect, 'byte')} of Body`,
      });
      break;
    }
  }

  if (idx !== size) {
    const bodyChunk = Buffer.alloc(size - idx);
    dataChunk.copy(bodyChunk, 0, idx, size);
    await collectBody(bodyChunk, client);
  }
}

/**
 *
 * @param {Buffer} dataChunk
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function collectBody(dataChunk, client) {
  const size = dataChunk.byteLength;
  if (size > client.toCollect) {
    await Logger.log({
      comment: `Received ${wAmount(size - client.toCollect, 'byte')} more then expected while collecting Body`
    });
  }

  const toAppend = Math.min(client.toCollect, size);
  const bodyPart = Buffer.alloc(toAppend);
  dataChunk.copy(bodyPart, 0, 0, toAppend);
  client.body = Buffer.concat([client.body, bodyPart]);
  client.toCollect -= toAppend;

  if (client.toCollect === 0) {
    await Logger.log({
      comment: 'Collected full body'
    });
    client.currentMessagePart = MESSAGE_PART.HEADER;
    client.headerChunkIdx = 0;
    client.isBodyCollected = true;
  }
}

/**
 *
 * @param {Buffer} dataChunk
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function handle(dataChunk, client) {
  await Logger.log({
    comment: `Received ${wAmount(dataChunk.byteLength, 'byte')} of data (collecting ${client.currentMessagePart})`
  });
  client.isBodyCollected = false;
  if (client.currentMessagePart === MESSAGE_PART.HEADER) {
    await collectHeader(dataChunk, client);
  } else {
    await collectBody(dataChunk, client);
  }

  if (!client.isBodyCollected) {
    return;
  }

  const deserializedBody = Slip.deserialize(client.body);
  const { data, status, action } = deserializedBody;
  const handlersDir = path.join(getParentDir(__dirname), 'action-handlers');
  await useHandlers(null, {
    makeExtraArgs: a => [{ action: a, data, client, status }],
    handlersDir,
    extractOne: action || '',
    occasionType: Logger.OCCASION_TYPE.action
  })();
}

module.exports = handle;