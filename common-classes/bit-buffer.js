class BitBuffer {
  data = null;

  /**
   *
   * @param {Array<number>} bits
   * @param {number} size
   * @return {Array<number>}
   */
  #complete(bits, size) {
    const toComplete = Math.max(0, size - bits.length);
    return [...Array(toComplete).fill(0), ...bits];
  }

  /**
   *
   * @param {number} num
   * @return {Array<number>}
   */
  #fromNumber(num) {
    return num.toString(2).split('').map(Number);
  }

  /**
   *
   * @param {Buffer} buf
   * @return {Array<number>}
   */
  #fromBuffer(buf) {
    return Array.from(buf.values()).map(byteVal => {
      const raw = this.#fromNumber(byteVal);
      return this.#complete(raw, 8);
    }).flat();
  }

  /**
   *
   * @param {string} str
   * @param {string=utf8} encoding
   * @return {Array<number>}
   */
  #fromString(str, encoding = 'utf8') {
    return this.#fromBuffer(Buffer.from(str, encoding));
  }

  /**
   *
   * @param {Buffer|number|string|Array<Buffer|number|string>} rawData
   * @param {number=} size
   * @param {number=} eachSize
   * @param {string=utf8} encoding
   * @return Array<number>
   */
  #makeData(rawData, { size, eachSize, encoding = 'utf8' } = {}) {
    let errorText = '';
    let bits;

    if (Buffer.isBuffer(rawData)) {
      bits = this.#fromBuffer(rawData);
      errorText = `${rawData.byteLength} bytes`;
    } else if (typeof rawData === 'string') {
      bits = this.#fromString(rawData, encoding);
      errorText = `"${rawData}"`;
    } else if (typeof rawData === 'number') {
      bits = this.#fromNumber(rawData);
      errorText = rawData;
    } else if (Array.isArray(rawData)) {
      const isBitsAlready = !rawData.some(item => ![0, 1].includes(item)) && eachSize == null;
      bits = isBitsAlready
        ? rawData
        : rawData.map(entry => this.#makeData(entry, { encoding, size: eachSize })).flat();

      errorText = 'data';
    } else {
      throw new Error(`Can't make a BitBuffer from ${rawData ? `a ${rawData.constructor.name}` : rawData}`);
    }

    const wSize = size != null;
    const bitLength = bits.length
    if (wSize && bitLength > size) {
      throw new Error(`Can't fit ${errorText} in ${size} bits`);
    }

    return wSize ? this.#complete(bits, size) : bits;
  }

  /**
   *
   * @param {BitBuffer|string|number|Array<string|number>}other
   * @return {Array<number>}
   */
  #useOther(other) {
    return other instanceof BitBuffer ? other.data : (new BitBuffer(other)).data;
  }

  constructor(rawData, { size, eachSize, encoding = 'utf8' } = {}) {
    this.data = this.#makeData(rawData, { size, eachSize, encoding });
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
    this.data.push(...this.#useOther(data));
  }

  prepend(data) {
    this.data.unshift(...this.#useOther(data));
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
   * @return {boolean}
   */
  startsWith(prefix) {
    for (const idx in prefix) {
      if (prefix[idx] !== this.data[idx]) {
        return false;
      }
    }

    return true;
  }

  /**
   *
   * @param {Array<BitBuffer>} bufList
   * @param {number=} totalSize
   * @return {BitBuffer}
   */
  static concat(bufList, totalSize) {
    return new BitBuffer(bufList.map(buf => buf.data).flat(), { size: totalSize });
  }
}

module.exports = BitBuffer;