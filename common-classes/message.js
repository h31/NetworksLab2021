const BitBuffer = require('./bit-buffer');
const ResourceRecord = require('./resource-record');
const { arrIntersectionSize } = require('../util/misc');
const { getAField } = require('../util/dns');
const { OPCODE } = require('../util/constants');
const TypedError = require('./typed-error');


/**
 * @typedef SectionItem
 * @type Object
 * @property {string} name
 * @property {Buffer} otherData
 */

class Message {
  static QR = {
    query: 0,
    response: 1
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
   * @param {Array<SectionItem>} questions
   * @param {Array<SectionItem>} answers
   * @param {Array<SectionItem>} authority
   * @param {Array<SectionItem>} additional
   * @return {Buffer}
   */
  static #compressMessage(questions, answers, authority, additional) {
    const processedNames = [];
    [questions, answers, authority, additional].forEach(section => {
      section.forEach(item => {
        const labels = item.name.split('.');
        const pointsTo = { idx: null, offset: 0 };

        processedNames.forEach((pn, idx) => {
          const intSize = arrIntersectionSize(labels, pn.labels);
          if (intSize > pointsTo.offset) {
            pointsTo.offset = intSize;
            pointsTo.idx = idx;
          }
        });

        processedNames.push({ labels, pointsTo, otherData: item.otherData });
      });
    });

    let currOffset = this.#headerByteSize;
    let compressionResult = Buffer.alloc(0);
    processedNames.forEach((pn, idx) => {
      const originalLength = pn.labels.length
      const pureUntil = originalLength - pn.pointsTo.offset;

      let totalLength = 0;
      const parts = pn.labels.slice(0, pureUntil).map(lbl => {
        if (!lbl) {
          return [];
        }
        const txtBuf = Buffer.from(lbl);
        const ln = txtBuf.byteLength;
        if (ln > 63) {
          throw new TypedError(
            `Encountered a label ${ln} bytes long while max is 63`,
            TypedError.TYPE.validation
          );
        }
        totalLength += ln + 1;
        const lnBuf = Buffer.from([ln]);
        return [lnBuf, txtBuf];
      }).flat();

      // no need to check if offset != null since it's NEVER null for pointers with non-zero offset
      let finisher;
      if (pureUntil === originalLength) {
        finisher = Buffer.from([0]);
        totalLength += 1;
      } else {
        const { labels: pnLabels, offsetFromStart: offsetFromMsgStart } = processedNames[pn.pointsTo.idx];
        const labelsMakingOffset = pnLabels.slice(0, pnLabels.length - pn.pointsTo.offset);
        const offsetFromNameStart = labelsMakingOffset.reduce((res, lbl) => res + 1 + Buffer.from(lbl).byteLength, 0);
        finisher = BitBuffer.concat([
          new BitBuffer([1, 1], { size: 2 }),
          new BitBuffer(offsetFromMsgStart + offsetFromNameStart, { size: 14 })
        ]).toBuffer();
        totalLength += 2;
      }

      if (totalLength > 255) {
        throw new TypedError(
          `Encountered a domain name ${totalLength} bytes long while max is 255`,
          TypedError.TYPE.validation
        );
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
   * @param {number} type
   * @param {number} klass
   * @return {SectionItem}
   */
  static #makeQuestion(domainName, type, klass) {
    const qTypeBuf = (new BitBuffer(type, { size: 16 })).toBuffer();
    const qClassBuf = (new BitBuffer(klass, { size: 16 })).toBuffer();
    return {
      name: domainName,
      otherData: Buffer.concat([qTypeBuf, qClassBuf])
    };
  }

  /**
   *
   * @param {object} data
   * @param {number} type
   * @param {number} klass
   * @param {number} ttl
   * @param {string} name
   * @return {SectionItem}
   */
  static #makeRR(data, type, klass, ttl, name) {
    const common = BitBuffer.concat([
      new BitBuffer(type, { size: 16 }),
      new BitBuffer(klass, { size: 16 }),
      new BitBuffer(ttl, { size: 32 })
    ]).toBuffer();

    let rDataBlock;

    switch (type) {
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
        const txtBuf = Buffer.from(data.text);
        const sizeBuf = (new BitBuffer(txtBuf.byteLength, { size: 16 })).toBuffer();
        rDataBlock = Buffer.concat([txtBuf, sizeBuf]);
        break;
      }
      case ResourceRecord.TYPE.mailExchange:
        const prefBuf = (new BitBuffer(+data.preference, { size: 16 })).toBuffer();
        const labels = data.exchange.split('.');
        let exchangeBuf = Buffer.alloc(0);
        labels.forEach(label => {
          const textBuf = Buffer.from(label);
          const lenBuf = Buffer.from([textBuf.byteLength]);
          exchangeBuf = Buffer.concat([exchangeBuf, lenBuf, textBuf]);
        });
        const rData = Buffer.concat([prefBuf, exchangeBuf, Buffer.from([0])]);
        const rdLengthBuf = (new BitBuffer(rData.byteLength, { size: 16 })).toBuffer();
        rDataBlock = Buffer.concat([rdLengthBuf, rData]);
        break;
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
   * @param {Array<{ name: string, type: number, class: number }>} questions
   * @param {number=} [opCode = 0]
   * @param {boolean=} [recDesired = true]
   * @return {Buffer}
   */
  static makeRequest(
    id, questions, {
      opCode = OPCODE.standardQuery,
      recDesired = true,
    } = {}
  ) {
    const sections = [...Array(4)].map(() => []);
    if (opCode === OPCODE.inverseQuery) {
      sections[1] = questions.map(q => {
        const field = getAField(q.type);
        return this.#makeRR({ [field]: q.name }, q.type, q.class, 0, '');
      });
    } else {
      sections[0] = questions.map(q => this.#makeQuestion(q.name, q.type, q.class));
    }
    const body = this.#compressMessage(...sections);

    // TODO: auto-trunc if message is too large
    const trunc = false;
    const header = this.#makeHeader(
      id, this.QR.query, opCode, 0,
      +trunc, +recDesired, 0, 0,
      sections[0].length, sections[1].length,
      0, 0
    );
    return Buffer.concat([header, body]);
  }

  static makeResponse(request, respCode, questions, answers, authority, additional) {
    const header = this.#makeHeader(
      request.id,
      this.QR.response,
      request.opCode,
      1,
      0,
      request.recDes,
      0, respCode, questions.length, answers.length, authority.length, additional.length
    );
    const sections = [
      questions.map(q => this.#makeQuestion(q.name, q.type, q.class)),
      ...[answers, authority, additional].map(sect =>
        sect.map(sItem => this.#makeRR(sItem.data, +sItem.type, +sItem.class, +sItem.ttl, sItem.name))
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
   * @return {{class: number, currOffset: number, name: string, type: number}}
   */
  static #parseQuestion(payload, startOffset) {
    const { name, currOffset } = this.#parseName(payload, startOffset);

    const typeAndClassBuf = Buffer.alloc(4);
    payload.copy(typeAndClassBuf, 0, currOffset, currOffset + 4);
    const [type, klass] = (new BitBuffer(typeAndClassBuf)).split([16, 16]).map(bb => bb.toNumber());

    return {
      name,
      type,
      class: klass,
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
   *   type: number,
   *   name: string,
   *   data: { a: string } | { text: string } | { aaaa: string } | { cName: string } | { preference: number, exchange: string },
   *   ttl: number,
   *   class: number
   * }}
   */
  static #parseRR(payload, startOffset) {
    const { name, currOffset } = this.#parseName(payload, startOffset);
    const configBuf = Buffer.alloc(10);
    payload.copy(configBuf, 0, currOffset, currOffset + 10);
    const [
      type, klass, ttl, rdLength
    ] = (new BitBuffer(configBuf)).split([16, 16, 32, 16]).map(bb => bb.toNumber());

    const rDataBuf = Buffer.alloc(rdLength);
    const rDataStart = currOffset + 10;
    const rDataEnd = rDataStart + rdLength;
    payload.copy(rDataBuf, 0, rDataStart, rDataEnd);
    let data = {};

    switch (type) {
      case ResourceRecord.TYPE.ipv4:
        data.a = Array.from(rDataBuf.values()).join('.');
        break;
      case ResourceRecord.TYPE.text:
        data.text = rDataBuf.toString();
        break;
      case ResourceRecord.TYPE.mailExchange:
        const [preferenceBuf] = (new BitBuffer(rDataBuf)).split([16]);
        const exchange = this.#parseName(payload, rDataStart + 2).name;
        data.preference = preferenceBuf.toNumber();
        data.exchange = exchange;
        break;
      case ResourceRecord.TYPE.ipv6:
        data.aaaa = (new BitBuffer(rDataBuf))
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
        data = {
          mName, rName,
          serial, refresh,
          retry, expire,
          minimum
        };
        break;
      case ResourceRecord.TYPE.canonicalName:
        data.cName = this.#parseName(payload, rDataStart).name;
    }

    return { name, type, class: klass, ttl, rdLength, data, currOffset: rDataEnd };
  }

  // === === === === === ===
  // PARSER
  // === === === === === ===

  /**
   *
   * @param {Buffer} payload
   */
  static parse(payload) {
    const parsed = {};

    try {
      if (!(payload instanceof Buffer)) {
        throw new Error(`Expected a Buffer, got ${payload.constructor.name}`);
      }

      const [
        id,
        qr, opCode, authAns, trunc, recDes, recAv, , rCode,
        qdCount, anCount, nsCount, arCount
      ] = this.#parseHeader(payload);
      Object.assign(parsed, {
        id,
        qr, opCode, authAns, trunc, recDes, recAv, rCode,
        qdCount, anCount, nsCount, arCount
      });

      const { results: questions, currOffset: qOffset } =
        this.#parseBunch(payload, this.#headerByteSize, qdCount, (...args) => this.#parseQuestion(...args));
      parsed.questions = questions;

      const { results: answers, currOffset: ansOffset } =
        this.#parseBunch(payload, qOffset, anCount, (...args) => this.#parseRR(...args));
      parsed.answers = answers;

      const { results: authority, currOffset: authOffset } =
        this.#parseBunch(payload, ansOffset, nsCount, (...args) => this.#parseRR(...args));
      parsed.authority = authority;

      const { results: additional } =
        this.#parseBunch(payload, authOffset, arCount, (...args) => this.#parseRR(...args))
      parsed.additional = additional;
    } catch (e) {
      parsed.error = e;
    }

    return parsed;
  }
}

module.exports = Message;