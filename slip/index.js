const SlipError = require('./error');
const { invert } = require('lodash');


class Slip {
  static TYPES = {
    date: 'd',
    string: 'l',
    number: 'n',
    slip: 's',
    file: 'f',
    boolean: 'b',
    array: 'a',
    none: 'x',
  };

  static usingRawBytes = [this.TYPES.slip, this.TYPES.array, this.TYPES.file];

  static escapeCharacters(str) {
    return str.replace(/[;|>]/g, substring => `>${substring}`);
  }

  static unEscapeCharacters(str) {
    return str.replace(/>;|>>|>\|/g, substring => substring[1]);
  }

  static findBorder(str, fromIndex, character) {
    let index = fromIndex;
    while (index < str.length) {
      const currCharacter = str[index];
      if (currCharacter === character) {
        return str.substring(fromIndex, index);
      }

      index += currCharacter === '>' ? 2 : 1;
    }

    return null;
  }

  static getDate(dateStr) {
    const isoFormat = /^(-\d{6})|(\d{4})-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$/;
    if (!isoFormat.test(dateStr)) {
      throw new SlipError('Dates must be in ISO format');
    }

    const date = new Date(dateStr);
    if (date.toString() === 'Invalid Date') {
      throw new SlipError(`Could not parse Date from ${dateStr}`);
    }

    return date;
  }

  static serialize(obj, files = {}) {
    let result = null;

    Object.entries(obj).forEach(([key, val]) => {
      let representation;
      let theType = this.TYPES[typeof val];
      let fileName = null;
      if (val == null) {
        representation = Buffer.alloc(0);
        theType = this.TYPES.none;
      } else if (val instanceof Date) {
        representation = Buffer.from(val.toISOString());
        theType = this.TYPES.date;
      } else if (Array.isArray(val)) {
        theType = this.TYPES.array;
        representation = this.serialize(val, files[key]);
      } else if (val instanceof Buffer) {
        const isFile = Object.keys(files).includes(key);
        representation = val;
        if (isFile) {
          fileName = files[key];
          theType = this.TYPES.file;
        } else {
          theType = this.TYPES.string;
        }
      } else if (val.constructor.name === 'Object') {
        representation = this.serialize(val, files[key]);
        theType = this.TYPES.slip;
      } else {
        switch (typeof val) {
          case 'string':
            representation = val;
            break;
          case 'number':
            representation = `${val}`;
            break;
          case 'boolean':
            representation = `${+val}`;
            break;
          default:
            throw new SlipError(`Can't serialize ${val.constructor.name} ${key}`);
        }
        representation = Buffer.from(representation);
      }

      const escKey = this.escapeCharacters(key);
      let sz;
      if ([this.TYPES.boolean, this.TYPES.none].includes(theType)) {
        sz = '';
      } else {
        sz = `${representation.byteLength}|`;
      }
      const serializedFileName = fileName ? this.escapeCharacters(fileName) : '';
      
      const toConcat = [Buffer.from(`${escKey}|${theType}${sz}`), Buffer.from(representation)];
      if (result) {
        toConcat.unshift(result);
      }
      if (serializedFileName) {
        toConcat.push(Buffer.from(serializedFileName));
      }
      toConcat.push(Buffer.from(';'));
      result = Buffer.concat(toConcat);
    });

    return result || Buffer.alloc(0);
  }

  static deserialize(payload) {
    let asString;
    if (payload instanceof Buffer) {
      asString = payload.toString();
    } else {
      throw new SlipError(`Expected a Buffer, got ${payload.constructor.name}`);
    }

    const result = {};
    let currentBufIndex = 0;
    let currentStrIndex = 0;

    const checkLength = () => {
      if (currentBufIndex > payload.byteLength) {
        throw new SlipError();
      }
    }

    const changeIdx = (change, str) => {
      currentStrIndex += change;
      currentBufIndex += change;
      if (str) {
        currentStrIndex += str.length;
        currentBufIndex += Buffer.from(str).byteLength;
      }
      checkLength();
    }

    while (currentBufIndex < payload.byteLength) {
      const rawKey = this.findBorder(asString, currentStrIndex, '|');
      if (rawKey == null) {
        throw new SlipError();
      }
      if (rawKey.length === 0) {
        throw new SlipError(`Key can't be empty; could only parse until token at position ${currentStrIndex}`);
      }
      // key + |
      changeIdx(1, rawKey);

      const key = this.unEscapeCharacters(rawKey);
      const type = invert(this.TYPES)[asString[currentStrIndex]];
      if (!type) {
        throw new SlipError({ str: asString, idx: currentStrIndex });
      }
      const typeSymbol = this.TYPES[type];
      // type
      changeIdx(1);

      let contentSize;
      if (typeSymbol === this.TYPES.boolean) {
        contentSize = 1;
      } else if (typeSymbol === this.TYPES.none) {
        contentSize = 0;
      } else {
        const contentSizeBorder = asString.indexOf('|', currentStrIndex);
        if (contentSizeBorder === -1) { // no border found
          throw new SlipError();
        }
        if (contentSizeBorder === 0) { // no content size provided
          throw new SlipError(`Content size is required for type ${type}`);
        }

        const contentSizeStr = asString.substring(currentStrIndex, contentSizeBorder);
        contentSize = parseInt(contentSizeStr);
        if (Number.isNaN(contentSize) || contentSizeStr.includes('.')) {
          throw new SlipError('Content size must be an Integer');
        }
        // size + |
        changeIdx(1, contentSizeStr);
      }

      const rawContent = Buffer.alloc(contentSize);
      payload.copy(rawContent, 0, currentBufIndex, currentBufIndex + contentSize);
      const rawContentAsString = rawContent.toString();

      switch (typeSymbol) {
        case this.TYPES.date:
          result[key] = this.getDate(rawContentAsString);
          break;
        case this.TYPES.string:
          result[key] = rawContentAsString;
          break;
        case this.TYPES.number:
          result[key] = +rawContentAsString;
          break;
        case this.TYPES.slip:
          result[key] = this.deserialize(rawContent);
          break;
        case this.TYPES.file:
          result[key] = { file: rawContent };
          break;
        case this.TYPES.boolean:
          if (!['0', '1'].includes(rawContentAsString)) {
            throw new SlipError(`Boolean must be defined as 0 or 1, got ${rawContentAsString}`);
          }
          result[key] = !!(+rawContentAsString);
          break;
        case this.TYPES.array:
          result[key] = Array.from(
            Object.entries(this.deserialize(rawContent))
              .sort(([keyA], [keyB]) => +keyA - +keyB)
              .map(([_, value]) => value)
          );
          break;
        case this.TYPES.none:
          result[key] = null;
          break;
      }

      if (!this.usingRawBytes.includes(typeSymbol)) {
        changeIdx(0, rawContentAsString);
      } else {
        currentStrIndex += rawContentAsString.length;
        currentBufIndex += contentSize;
        checkLength();
      }

      // has file name
      if (asString[currentStrIndex] !== ';') {
        if (typeSymbol !== this.TYPES.file) {
          throw new SlipError(`${key} is defined as ${type}, but has a file name`);
        }
        const rawFileName = this.findBorder(asString, currentStrIndex, ';');
        if (rawFileName == null) {
          throw new SlipError(`Failed while extracting the file name for ${key}`);
        }
        result[key].name = this.unEscapeCharacters(rawFileName);
        // fileName
        changeIdx(0, rawFileName);
      } else if (typeSymbol === this.TYPES.file) {
        throw new SlipError(`${key} is defined as a File, but has no file name`);
      }

      // ;
      changeIdx(1);
    }

    return result;
  }
}

module.exports = Slip;