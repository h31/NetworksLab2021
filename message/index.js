const BitBuffer = require('../bit-buffer/index');
const ResourceRecord = require('../resource-record/index');
const { arrIntersectionSize } = require('../util/misc');


/**
 * @typedef SectionItem
 * @type Object
 * @property {string} name
 * @property {Buffer} otherData
 */
/**
 * @typedef CompressConfig
 * @type Object
 * @property {Array<SectionItem>} sectionData
 * @property {boolean} noCompress
 */

class Message {
  static QR = {
    query: 0,
    response: 1
  };

  static OPCODE = {
    standardQuery: 0,
    inverseQuery: 1,
    status: 2
  };

  static RCODE = {
    noError: 0,
    formatError: 1,
    serverFailure: 2,
    nameError: 3,
    notImplemented: 4,
    refused: 5
  };

  /**
   *
   * @private
   */
  static _headerBitSizes = [16, 1, 4, 1, 1, 1, 1, 3, 4, 16, 16, 16, 16];
  /**
   *
   * @private
   */
  static _headerByteSize = 12;

  // === === === === === ===
  // MAKE UTILS
  // === === === === === ===

  /**
   *
   * @param {CompressConfig} questions
   * @param {CompressConfig} answers
   * @param {CompressConfig} authority
   * @param {CompressConfig} additional
   * @return {Buffer}
   * @private
   */
  static _compressMessage(questions, answers, authority, additional) {
    const processedNames = [];
    [questions, answers, authority, additional].forEach(section => {
      section.sectionData.forEach(item => {
        const labels = item.name.split('.');
        const pointsTo = { idx: null, offset: 0 };

        if (!section.noCompress) {
          processedNames.forEach((pn, idx) => {
            const intSize = arrIntersectionSize(labels, pn.labels);
            if (intSize > pointsTo.offset) {
              pointsTo.offset = intSize;
              pointsTo.idx = idx;
            }
          });
        }

        processedNames.push({ labels, pointsTo, otherData: item.otherData });
      });
    });

    let currOffset = this._headerByteSize;
    let compressionResult = Buffer.alloc(0);
    processedNames.forEach((pn, idx) => {
      const originalLength = pn.labels.length
      const pureUntil = originalLength - pn.pointsTo.offset;

      const parts = pn.labels.slice(0, pureUntil).map(lbl => {
        const buf = Buffer.from(lbl);
        const ln = Buffer.from([buf.byteLength]);
        return [ln, buf];
      }).flat();

      // no need to check if offset != null since it's NEVER null for pointers with non-zero offset
      let finisher;
      if (pureUntil === originalLength) {
        finisher = Buffer.from([0]);
      } else {
        const { labels: poLabels, offsetFromStart: offsetFromMsgStart } = processedNames[pn.pointsTo.idx];
        const labelsMakingOffset = poLabels.slice(0, poLabels.length - pn.pointsTo.offset);
        const offsetFromNameStart = labelsMakingOffset.reduce((res, lbl) => res + 1 + lbl.length, 0);
        finisher = BitBuffer.concat([
          new BitBuffer([1, 1], { size: 2 }),
          new BitBuffer(offsetFromMsgStart + offsetFromNameStart, { size: 14 })
        ]).toBuffer();
      }
      parts.push(finisher, pn.otherData);

      processedNames[idx].offsetFromStart = currOffset;

      const bufDataForItem = Buffer.concat(parts);
      currOffset += bufDataForItem.byteLength;

      compressionResult = Buffer.concat([compressionResult, bufDataForItem]);
    });

    return compressionResult;
  }

  // === === === === === ===
  // PART MAKERS
  // === === === === === ===

  /**
   *
   * @param {number} id
   * @param {number} qr
   * @param {number} opCode
   * @param {number} authAnswer
   * @param {number} trunc
   * @param {number} recDesired
   * @param {number} recAvail
   * @param {number} respCode
   * @param {number} qdCount
   * @param {number} anCount
   * @param {number} nsCount
   * @param {number} arCount
   * @return {Buffer}
   * @private
   */
  static _makeHeader(id, qr, opCode, authAnswer, trunc, recDesired, recAvail, respCode, qdCount, anCount, nsCount, arCount) {
    return BitBuffer.concat([
        id, qr, opCode, authAnswer,
        trunc, recDesired, recAvail, 0,
        respCode, qdCount, anCount, nsCount, arCount
      ].map((value, index) => new BitBuffer(value, { size: this._headerBitSizes[index] })),
      this._headerByteSize * 8 // to make sure everything is correct
    ).toBuffer();
  }

  /**
   *
   * @param {string} domainName
   * @param {number} qType
   * @param {number} qClass
   * @return {SectionItem}
   * @private
   */
  static _makeQuestion(domainName, qType, qClass) {
    const qTypeBuf = (new BitBuffer(qType, { size: 16 })).toBuffer();
    const qClassBuf = (new BitBuffer(qClass, { size: 16 })).toBuffer();
    return {
      name: domainName,
      otherData: Buffer.concat([qTypeBuf, qClassBuf])
    };
  }

  // TODO: data type ???
  /**
   *
   * @param {string} data
   * @param {number} rrType
   * @param {number} rrClass
   * @param {number} ttl
   * @param {boolean=false} inverse
   * @return {SectionItem}
   * @private
   */
  static _makeRR(data, rrType, rrClass, ttl, inverse) {
    // TODO
  }

  // === === === === === ===
  // MAKE
  // === === === === === ===

  /**
   *
   * @param {number} id
   * @param {Array<string>} questions
   * @param {{
   *   opCode: number = 0,
   *   recDesired: boolean = true,
   *   qType: number = 1,
   *   qClass: number = 1
   * }} config
   * @return {Buffer}
   */
  static makeRequest(
    id, questions, {
      opCode= this.OPCODE.standardQuery,
      recDesired= true,
      qType= ResourceRecord.TYPE.ipv4,
      qClass= ResourceRecord.CLASS.internet
    } = {}
  ) {
    const sections = [...Array(4)].map(() => ({ noCompress: false, sectionData: [] }));
    if (opCode === this.OPCODE.inverseQuery) {
      sections[1].sectionData = questions.map(q => this._makeRR(q, qType, qClass, 0, true))
    } else {
      sections[0].sectionData = questions.map(q => this._makeQuestion(q, qType, qClass));
    }
    const body = this._compressMessage(...sections);

    // TODO: auto-trunc if message is too large
    const trunc = false;
    const header = this._makeHeader(
      id, this.QR.query, opCode, 0,
      +trunc, +recDesired, 0, 0,
      questions.length, 0, 0, 0
    );
    return Buffer.concat([header, body]);
  }

  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

  // === === === === === ===
  // PARSE UTILS
  // === === === === === ===

  /**
   *
   * @param {Buffer} payload
   * @param {number} startOffset
   * @return {{currOffset: number, name: string}}
   * @private
   */
  static _parseName(payload, startOffset) {
    let currOffset = startOffset;
    const qNameParts = [];
    let labelSize;
    let isPointer;
    do {
      labelSize = payload[currOffset];
      if (labelSize == null) {
        break;
      }
      isPointer = (new BitBuffer(labelSize, { size: 8 })).startsWith([1, 1]);
      if (isPointer) {
        const pointerDataBuf = Buffer.alloc(2);
        payload.copy(pointerDataBuf, 0, currOffset, currOffset + 2);
        const [, pointerVal] = (new BitBuffer(pointerDataBuf)).split([2, 14]);
        currOffset += 2;
        qNameParts.push(this._parseName(payload, pointerVal.toNumber()).name);
      } else {
        currOffset++;

        if (labelSize) {
          const labelBuf = Buffer.alloc(labelSize);
          payload.copy(labelBuf, 0, currOffset, currOffset + labelSize);
          qNameParts.push(labelBuf.toString('utf8'));
          currOffset += labelSize;
        }
      }
    } while (labelSize && !isPointer);

    return {
      name: qNameParts.join('.'),
      currOffset
    };
  }

  /**
   *
   * @param {Buffer} payload
   * @param {number} startOffset
   * @param {number} count
   * @param {function(Buffer, number): Object} parser
   * @return {{currOffset: number, results: Array<Object>}}
   * @private
   */
  static _parseBunch(payload, startOffset, count, parser) {
    const results = [];
    let resultsLeft = count;
    let currOffset = startOffset;
    while (resultsLeft) {
      resultsLeft--;

      const { currOffset: newOffset, ...rest } = parser(payload, currOffset);
      currOffset = newOffset;
      results.push({ ...rest });
    }

    return {
      results,
      currOffset
    };
  }

  // === === === === === ===
  // PART PARSERS
  // === === === === === ===

  /**
   *
   * @param {Buffer} payload
   * @return {Array<number>}
   * @private
   */
  static _parseHeader(payload) {
    return (new BitBuffer(payload)).split(this._headerBitSizes).map(bb => bb.toNumber());
  }

  /**
   *
   * @param {Buffer} payload
   * @param {number} startOffset
   * @return {{qClass: number, currOffset: number, name: string, qType: number}}
   * @private
   */
  static _parseQuestion(payload, startOffset) {
    const { name, currOffset } = this._parseName(payload, startOffset);

    const typeAndClassBuf = Buffer.alloc(4);
    payload.copy(typeAndClassBuf, 0, currOffset, currOffset + 4);
    const [qType, qClass] = (new BitBuffer(typeAndClassBuf)).split([16, 16]).map(bb => bb.toNumber());

    return {
      name,
      qType,
      qClass,
      currOffset: currOffset + 4
    };
  }

  /**
   *
   * @param {Buffer} payload
   * @param {number} startOffset
   * @return {{
   *   rdLength: number,
   *   currOffset: number,
   *   rrType: number,
   *   name: string,
   *   rData: string | {preference: number, exchange: string},
   *   ttl: number,
   *   rrClass: number
   * }}
   * @private
   */
  static _parseRR(payload, startOffset) {
    const { name, currOffset } = this._parseName(payload, startOffset);
    const configBuf = Buffer.alloc(10);
    payload.copy(configBuf, 0, currOffset, currOffset + 10);
    const [
      rrType, rrClass, ttl, rdLength
    ] = (new BitBuffer(configBuf)).split([16, 16, 32, 16]).map(bb => bb.toNumber());

    const rDataBuf = Buffer.alloc(rdLength);
    payload.copy(rDataBuf, 0, currOffset + 10, currOffset + 10 + rdLength);
    let rData;
    switch (rrType) {
      case ResourceRecord.TYPE.ipv4:
        rData = Array.from(rDataBuf.values()).join('.');
        break;
      case ResourceRecord.TYPE.text:
        rData = rDataBuf.toString();
        break;
      case ResourceRecord.TYPE.mailExchange:
        const [preferenceBuf] = (new BitBuffer(rDataBuf)).split([16]);
        const exchange = Array.from(rDataBuf.values()).slice(2).join('.');
        rData = {
          preference: preferenceBuf.toNumber(),
          exchange
        };
        break;
      case ResourceRecord.TYPE.ipv6:
        rData = (new BitBuffer(rDataBuf))
          .split([16, 16, 16, 16])
          .map(bb => bb.toNumber().toString(16))
          .join('.');
        break;
    }

    return { name, rrType, rrClass, ttl, rdLength, rData, currOffset: currOffset + 10 + rdLength };
  }

  // === === === === === ===
  // PARSER
  // === === === === === ===

  /**
   *
   * @param {Buffer} payload
   */
  static parse(payload) {
    if (!(payload instanceof Buffer)) {
      throw new Error(`Expected a Buffer, got ${payload.constructor.name}`);
    }

    const [
      id,
      qr, opCode, authAns, trunc, recDes, recAv, , rCode,
      qdCount, anCount, nsCount, arCount
    ] = this._parseHeader(payload);

    const { results: questions, currOffset: qOffset } =
      this._parseBunch(payload, this._headerByteSize, qdCount, (...args) => this._parseQuestion(...args));

    const { results: answers, currOffset: ansOffset } =
      this._parseBunch(payload, qOffset, anCount, (...args) => this._parseRR(...args));

    const { results: authority, currOffset: authOffset } =
      this._parseBunch(payload, ansOffset, nsCount, (...args) => this._parseRR(...args));

    const { results: additional } =
      this._parseBunch(payload, authOffset, arCount, (...args) => this._parseRR(...args))

    return {
      id,
      qr, opCode, authAns, trunc, recDes, recAv, rCode,
      qdCount, anCount, nsCount, arCount,
      questions, answers, authority, additional
    };
  }
}

module.exports = Message;