class BitBuffer {
  data = null;

  /**
   *
   * @param {Array<number>} bits
   * @param {number} size
   * @return {Array<number>}
   * @private
   */
  _complete(bits, size) {
    const toComplete = Math.max(0, size - bits.length);
    return [...Array(toComplete).fill(0), ...bits];
  }

  /**
   *
   * @param {number} num
   * @return {Array<number>}
   * @private
   */
  _fromNumber(num) {
    return num.toString(2).split('').map(Number);
  }

  /**
   *
   * @param {Buffer} buf
   * @return {Array<number>}
   * @private
   */
  _fromBuffer(buf) {
    return Array.from(buf.values()).map(byteVal => {
      const raw = this._fromNumber(byteVal);
      return this._complete(raw, 8);
    }).flat();
  }

  /**
   *
   * @param {string} str
   * @param {string=utf8} encoding
   * @return {Array<number>}
   * @private
   */
  _fromString(str, encoding = 'utf8') {
    return this._fromBuffer(Buffer.from(str, encoding));
  }

  /**
   *
   * @param {Buffer|number|string|Array<Buffer|number|string>} rawData
   * @param {number=} size
   * @param {number=} eachSize
   * @param {string=utf8} encoding
   * @return {Array<number>}
   * @private
   */
  _makeData(rawData, { size, eachSize, encoding = 'utf8' } = {}) {
    let errorText = '';
    let bits;

    if (Buffer.isBuffer(rawData)) {
      bits = this._fromBuffer(rawData);
      errorText = `${rawData.byteLength} bytes`;
    } else if (typeof rawData === 'string') {
      bits = this._fromString(rawData, encoding);
      errorText = `"${rawData}"`;
    } else if (typeof rawData === 'number') {
      bits = this._fromNumber(rawData);
      errorText = rawData;
    } else if (Array.isArray(rawData)) {
      const isBitsAlready = !rawData.some(item => ![0, 1].includes(item));
      bits = isBitsAlready
        ? rawData
        : rawData.map(entry => this._makeData(entry, { encoding, size: eachSize })).flat();

      errorText = 'data';
    } else {
      throw new Error(`Can't make a BitBuffer from ${rawData ? `a ${rawData.constructor.name}` : rawData}`);
    }

    const wSize = size != null;
    const bitLength = bits.length
    if (wSize && bitLength > size) {
      throw new Error(`Can't fit ${errorText} in ${size} bits`);
    }

    return wSize ? this._complete(bits, size) : bits;
  }

  /**
   *
   * @param {BitBuffer|string|number|Array<string|number>}other
   * @return {Array<number>}
   * @private
   */
  _useOther(other) {
    return other instanceof BitBuffer ? other.data : (new BitBuffer(other)).data;
  }

  constructor(rawData, { size, eachSize, encoding = 'utf8' } = {}) {
    this.data = this._makeData(rawData, { size, eachSize, encoding });
  }

  toNumber() {
    return Number(`0b${this.data.join('')}`);
  }

  toBuffer() {
    const binaryArray = [];
    let sliceEnd = this.data.length;
    while (sliceEnd > 0) {
      const sliceStart = Math.max(0, sliceEnd - 8);
      const bitsForByte = this.data.slice(sliceStart, sliceEnd);
      sliceEnd -= 8;
      const asBinary = `0b${bitsForByte.join('')}`;
      binaryArray.unshift(Number(asBinary));
    }

    return Buffer.from(binaryArray);
  }

  toString(encoding = 'utf8') {
    return this.toBuffer().toString(encoding);
  }

  append(data) {
    this.data.push(...this._useOther(data));
  }

  prepend(data) {
    this.data.unshift(...this._useOther(data));
  }

  /**
   *
   * @param {Array<number>} bitSizes
   * @return {Array<BitBuffer>}
   */
  split(bitSizes) {
    let sliceStart = 0;
    const bitSlices = [];
    bitSizes.forEach(size => {
      bitSlices.push(new BitBuffer(this.data.slice(sliceStart, sliceStart + size)));
      sliceStart += size;
    });

    return bitSlices;
  }

  /**
   *
   * @param {Array<number>} prefix
   */
  startsWith(prefix) {
    for (const idx in prefix) {
      if (prefix[idx] !== this.data[idx]) {
        return false;
      }
    }

    return true;
  }

  static concat(bufList, totalSize) {
    return new BitBuffer(bufList.map(buf => buf.data).flat(), { size: totalSize });
  }
}

module.exports = BitBuffer;