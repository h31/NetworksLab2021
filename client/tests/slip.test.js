const SlipHandler = require('../slip');
const fs = require('fs');
const path = require('path');
const { set, get } = require('lodash');


const barashPayload = {
  user: 'Barash',
  message: 'Беее...',
  profileData: { created: new Date(2002, 11, 20) },
};

const somebodyPayload = {
  band: 'Smash Mouth',
  date: new Date(2005, 8, 23),
  time: '3.20'
};

/**
 *
 * @param {number= } [max = 1024]
 * @return {number}
 */
function getRandomInt(max = 1024) {
  return Math.floor(Math.random() * max);
}

/**
 *
 * Imitate receiving data in chunks
 * @param {Buffer} buf
 * @param {number=} [max = 1024] max size for each chunk
 * @return {Array<Buffer>}
 */
function imitateChunks(buf, max = 1024) {
  const chunks = [];
  const len = buf.byteLength;
  let offset = 0;
  while (len - offset > 0) {
    const chunkSize = Math.min(getRandomInt(max), len - offset);
    const chunk = Buffer.alloc(chunkSize);
    buf.copy(chunk, 0, offset, offset + chunkSize);
    offset += chunkSize;
    chunks.push(chunk);
  }
  return chunks;
}

/**
 *
 * Get a fresh SlipHandler and serialize some object with it
 * @param {Object} data
 * @param {Object=} files
 * @return {{serializedData: Buffer, slipHandler: SlipHandler}}
 */
function init(data, files) {
  const slipHandler = new SlipHandler();
  const serializedData = slipHandler.makeMessage(data, files);
  return { slipHandler, serializedData };
}

/**
 *
 * Deserialize some data with an existing SlipHandler
 * @param {SlipHandler} slipHandler
 * @param {Buffer} buf
 * @param {number=} [max = 1024] `max` argument for {@link imitateChunks}
 * @return {Buffer}
 */
function useChunks(slipHandler, buf, max = 1024) {
  const chunks = imitateChunks(buf, max);
  let deserializedData;
  for (const chunk of chunks) {
    deserializedData = slipHandler.feed(chunk);
  }
  return deserializedData;
}

/**
 *
 * Make a Slip Message from a string imitating an already serialized body
 * Also returns a fresh SlipHandler, since the previous one might be dirty after throwing an error
 * @param {string} str
 * @return {{ pseudoMessage: Buffer, slipHandler: SlipHandler }}
 */
function imitateMessage(str) {
  const pseudoBody = Buffer.from(str);
  const pseudoHeader = SlipHandler.makeHeader(pseudoBody);
  return { pseudoMessage: Buffer.concat([pseudoHeader, pseudoBody]), slipHandler: new SlipHandler() };
}

/**
 *
 * @param {*} done
 * @param {string} fileName
 * @param {Object} payload
 * @param {string | Array<string | number>}pathInPayload
 * @param {SlipHandler=} oldSlipHandler to test that everything works without creating a fresh SlipHandler for each message
 */
function withFile(done, fileName, payload, pathInPayload, oldSlipHandler) {
  const runTest = async () => {
    try {
      await fs.promises.mkdir(path.join(__dirname, 'parsed-files'));
    } catch {}
    const pathToFile = path.join(__dirname, 'original-files', fileName);
    const fileData = await fs.promises.readFile(pathToFile, { encoding: null });

    const toSerialize = { ...payload };
    set(toSerialize, pathInPayload, fileData);
    const files = {};
    set(files, pathInPayload, fileName);

    const toUse = init(toSerialize, files);
    const serializedData = toUse.serializedData;
    const slipHandler = oldSlipHandler || toUse.slipHandler;
    const deserializedData = useChunks(slipHandler, serializedData, 2048);

    const toCompare = { ...payload };
    set(toCompare, pathInPayload, { file: fileData, name: fileName })
    expect(deserializedData).toEqual(toCompare);

    const pathToParsedFile = path.join(__dirname, 'parsed-files', get(deserializedData, pathInPayload).name);
    await fs.promises.writeFile(pathToParsedFile, get(deserializedData, pathInPayload).file);
  }

  runTest().then(() => done()).catch(e => done(e));
}

describe(
  'SlipHandler', () => {
    it('Should make correct headers', () => {
      const data = Buffer.from([...Array(30000)].map((_, i) => i % 256));
      expect(SlipHandler.makeHeader(data).equals(Buffer.from([0xb0, 0xea, 0x1]))).toBeTruthy();
    });

    it('Should serialize data properly', () => {
      const payload = {
        str: 'A String',
        num: 12,
        none: null,
        emptyObj: {  },
        encodedStr: 'Привет! Ô'
      };
      const { serializedData } = init(payload);
      // N is the Header: one byte containing number 78
      expect(serializedData.toString()).toBe(
        'Nstr|l8|A String;num|n2|12;none|x;emptyObj|s0|;encodedStr|l16|Привет! Ô;'
      );
    });

    it('Should deserialize data properly', () => {
      const payload = {
        '>strange->key': 12.43,
        data: { idx: 12, val: null, when: new Date() },
        // To ensure strange encodings don't mess everything up
        message: 'Привет! Ô',
        isRead: false,
        '|shed|': '|-|-|-|-|-|-|',
        yaharr: [1, 2, { obj: 'with-one-field' }]
      };

      const { serializedData, slipHandler } = init(payload);
      const deserializedData = useChunks(slipHandler, serializedData)
      expect(deserializedData).toEqual(payload);
    });

    it('Should not deserialize invalid data, should throw errors with proper messages', () => {
      let slipHandler;

      const emptyKey = imitateMessage('firstKey|l4|wooo;|b1');
      slipHandler = emptyKey.slipHandler;
      expect(() => slipHandler.feed(emptyKey.pseudoMessage)).toThrow(
        'Key can\'t be empty; could only parse until token at position 17'
      );

      const invalidBool = imitateMessage('isGood|b1;isBad|b8;');
      slipHandler = invalidBool.slipHandler;
      expect(() => slipHandler.feed(invalidBool.pseudoMessage)).toThrow(
        'Boolean must be defined as 0 or 1, got 8'
      );

      const fileNameWithoutFile = imitateMessage('str|l5|Hello;num|n1|5the-file-no-one-needs.txt;');
      slipHandler = fileNameWithoutFile.slipHandler;
      expect(() => slipHandler.feed(fileNameWithoutFile.pseudoMessage)).toThrow(
        'num is defined as number, but has a file name'
      );

      const fileWithoutFileName = imitateMessage('str|l5|Hello;doc|f8|12345678;');
      slipHandler = fileWithoutFileName.slipHandler;
      expect(() => slipHandler.feed(fileWithoutFileName.pseudoMessage)).toThrow(
        'doc is defined as a File, but has no file name'
      );

      const nonIsoDate = imitateMessage('who|l2|me;dob|d10|2000-07-17;');
      slipHandler = nonIsoDate.slipHandler;
      expect(() => slipHandler.feed(nonIsoDate.pseudoMessage)).toThrow(
        'Dates must be in ISO format'
      );

      const invalidDate = imitateMessage('suggestedBy|l12|Münchhausen;whatSuggested|d24|1979-05-32T12:00:00.000Z');
      slipHandler = invalidDate.slipHandler;
      expect(() => slipHandler.feed(invalidDate.pseudoMessage)).toThrow(
        'Could not parse Date from 1979-05-32T12:00:00.000Z'
      );
    });

    it('Should read data with attached text files', done => {
      withFile(done, 'doc.txt', somebodyPayload, 'lyrics');
    });

    it('Should read data with attached image files', done => {
      withFile(done, 'good.jpg', barashPayload, 'profileData.avatar');
    });

    it('Should parse several Messages with the same handler', done => {
      const slipHandler = new SlipHandler();
      withFile(done, 'doc.txt', somebodyPayload, 'lyrics', slipHandler);
      withFile(done, 'good.jpg', barashPayload, 'profileData.avatar', slipHandler);
    });
  }
);