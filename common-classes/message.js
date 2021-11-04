const BitBuffer = require('./bit-buffer');
const ResourceRecord = require('./resource-record');
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

  static #headerBitSizes = [16, 1, 4, 1, 1, 1, 1, 3, 4, 16, 16, 16, 16];
  static #headerByteSize = 12;

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
   */
  static #compressMessage(questions, answers, authority, additional) {
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

    let currOffset = this.#headerByteSize;
    let compressionResult = Buffer.alloc(0);
    processedNames.forEach((pn, idx) => {
      const originalLength = pn.labels.length
      const pureUntil = originalLength - pn.pointsTo.offset;

      const parts = pn.labels.slice(0, pureUntil).map(lbl => {
        if (!lbl) {
          return [];
        }
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
        const offsetFromNameStart = labelsMakingOffset.reduce((res, lbl) => res + 1 + Buffer.from(lbl).byteLength, 0);
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
   */
  static #makeHeader(id, qr, opCode, authAnswer, trunc, recDesired, recAvail, respCode, qdCount, anCount, nsCount, arCount) {
    return BitBuffer.concat([
        id, qr, opCode, authAnswer,
        trunc, recDesired, recAvail, 0,
        respCode, qdCount, anCount, nsCount, arCount
      ].map((value, index) => new BitBuffer(value, { size: this.#headerBitSizes[index] })),
      this.#headerByteSize * 8 // to make sure everything is correct
    ).toBuffer();
  }

  /**
   *
   * @param {string} domainName
   * @param {number} qType
   * @param {number} qClass
   * @return {SectionItem}
   */
  static #makeQuestion(domainName, qType, qClass) {
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
   * @param {object} data
   * @param {number} rrType
   * @param {number} rrClass
   * @param {number} ttl
   * @param {string} name
   * @return {SectionItem}
   */
  static #makeRR(data, rrType, rrClass, ttl, name) {
    const common = BitBuffer.concat([
      new BitBuffer(rrType, { size: 16 }),
      new BitBuffer(rrClass, { size: 16 }),
      new BitBuffer(ttl, { size: 32 })
    ]).toBuffer();

    let rDataBlock;

    switch (rrType) {
      case ResourceRecord.TYPE.ipv4: {
        const addrParts = data.a.split('.').map(Number);
        rDataBlock = Buffer.from([0, 4, ...addrParts]);
        break;
      }
      case ResourceRecord.TYPE.ipv6: {
        const addrParts = data.aaaa.split(':').map(v => Number(`0x${v}`));
        const addrPartsBuf = (new BitBuffer(addrParts, { eachSize: 16 })).toBuffer();
        rDataBlock = Buffer.concat([
          Buffer.from([0, 16]),
          addrPartsBuf
        ]);
        break;
      }
      case ResourceRecord.TYPE.text: {
        const txtBuf = Buffer.from(data);
        const sizeBuf = (new BitBuffer(txtBuf.byteLength, { size: 16 })).toBuffer();
        rDataBlock = Buffer.concat([txtBuf, sizeBuf]);
        break;
      }
      case ResourceRecord.TYPE.mailExchange:
      // TODO
    }

    return {
      name,
      otherData: Buffer.concat([common, rDataBlock])
    };

  }

  // === === === === === ===
  // MAKE
  // === === === === === ===

  /**
   *
   * @param {number} id
   * @param {Array<string>} questions
   * @param {number=} [opCode = 0]
   * @param {boolean=} [recDesired = true]
   * @param {number=} [qType = 1]
   * @param {number} [qClass = 1]
   * @return {Buffer}
   */
  static makeRequest(
    id, questions, {
      opCode = this.OPCODE.standardQuery,
      recDesired = true,
      qType= ResourceRecord.TYPE.ipv4,
      qClass= ResourceRecord.CLASS.internet
    } = {}
  ) {
    const sections = [...Array(4)].map(() => ({ noCompress: false, sectionData: [] }));
    if (opCode === this.OPCODE.inverseQuery) {
      const field = qType === ResourceRecord.TYPE.ipv4 ? 'a' : 'aaaa';
      sections[1].sectionData = questions.map(q => this.#makeRR({ [field]: q }, qType, qClass, 0, ''))
    } else {
      sections[0].sectionData = questions.map(q => this.#makeQuestion(q, qType, qClass));
    }
    const body = this.#compressMessage(...sections);

    // TODO: auto-trunc if message is too large
    const trunc = false;
    const header = this.#makeHeader(
      id, this.QR.query, opCode, 0,
      +trunc, +recDesired, 0, 0,
      sections[0].sectionData.length, sections[1].sectionData.length,
      0, 0
    );
    return Buffer.concat([header, body]);
  }

  // return {
  //       id,
  //       qr, opCode, authAns, trunc, recDes, recAv, rCode,
  //       qdCount, anCount, nsCount, arCount,
  //       questions, answers, authority, additional
  //     };
  //  * @return {{qClass: number, currOffset: number, name: string, qType: number}}

  static makeResponse(request, answers, authority, additional) {
    const header = this.#makeHeader(
      request.id,
      this.QR.response,
      request.opCode,
      1,
      0,
      request.recDes,
      1, 0, request.qdCount, answers.length, authority.length, additional.length
    );
    const sections = [
      { noCompress: true, sectionData: request.questions.map(q => this.#makeQuestion(q.name, q.qType, q.qClass)) },
      ...[answers, authority, additional].map(sect =>
        ({ noCompress: false, sectionData: sect.map(sItem => this.#makeRR(sItem.data, +sItem.type, +sItem.class, +sItem.ttl, sItem.name)) })
      )
    ];

    const body = this.#compressMessage(...sections);
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
   */
  static #parseName(payload, startOffset) {
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
        qNameParts.push(this.#parseName(payload, pointerVal.toNumber()).name);
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
   */
  static #parseBunch(payload, startOffset, count, parser) {
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
   */
  static #parseHeader(payload) {
    return (new BitBuffer(payload)).split(this.#headerBitSizes).map(bb => bb.toNumber());
  }

  /**
   *
   * @param {Buffer} payload
   * @param {number} startOffset
   * @return {{qClass: number, currOffset: number, name: string, qType: number}}
   */
  static #parseQuestion(payload, startOffset) {
    const { name, currOffset } = this.#parseName(payload, startOffset);

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
   */
  static #parseRR(payload, startOffset) {
    const { name, currOffset } = this.#parseName(payload, startOffset);
    const configBuf = Buffer.alloc(10);
    payload.copy(configBuf, 0, currOffset, currOffset + 10);
    const [
      rrType, rrClass, ttl, rdLength
    ] = (new BitBuffer(configBuf)).split([16, 16, 32, 16]).map(bb => bb.toNumber());

    const rDataBuf = Buffer.alloc(rdLength);
    const rDataStart = currOffset + 10;
    const rDataEnd = rDataStart + rdLength;
    payload.copy(rDataBuf, 0, rDataStart, rDataEnd);
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
        const exchange = this.#parseName(payload, rDataStart + 2).name;
        rData = {
          preference: preferenceBuf.toNumber(),
          exchange
        };
        break;
      case ResourceRecord.TYPE.ipv6:
        rData = (new BitBuffer(rDataBuf))
          .split([16, 16, 16, 16, 16, 16, 16, 16])
          .map(bb => bb.toNumber().toString(16))
          .join(':');
        break;
      case ResourceRecord.TYPE.startOfAuthority:
        const { currOffset: mNameOffset, name: mName } = this.#parseName(payload, rDataStart);
        const { currOffset: rNameOffset, name: rName } = this.#parseName(payload, mNameOffset);
        const otherDataBuf = Buffer.alloc(20);
        payload.copy(otherDataBuf, 0, rNameOffset, rNameOffset + 20);
        const [serial, refresh, retry, expire, minimum] =
          (new BitBuffer(otherDataBuf)).split([32, 32, 32, 32, 32]).map(bb => bb.toNumber());
        rData = {
          mName, rName,
          serial, refresh,
          retry, expire,
          minimum
        };
        break;
      case ResourceRecord.TYPE.canonicalName:
        rData = this.#parseName(payload, rDataStart).name;
    }

    return { name, rrType, rrClass, ttl, rdLength, rData, currOffset: rDataEnd };
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
    ] = this.#parseHeader(payload);

    const { results: questions, currOffset: qOffset } =
      this.#parseBunch(payload, this.#headerByteSize, qdCount, (...args) => this.#parseQuestion(...args));

    const { results: answers, currOffset: ansOffset } =
      this.#parseBunch(payload, qOffset, anCount, (...args) => this.#parseRR(...args));

    const { results: authority, currOffset: authOffset } =
      this.#parseBunch(payload, ansOffset, nsCount, (...args) => this.#parseRR(...args));

    const { results: additional } =
      this.#parseBunch(payload, authOffset, arCount, (...args) => this.#parseRR(...args))

    return {
      id,
      qr, opCode, authAns, trunc, recDes, recAv, rCode,
      qdCount, anCount, nsCount, arCount,
      questions, answers, authority, additional
    };
  }
}

module.exports = Message;