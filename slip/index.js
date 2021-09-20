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

  static serialize(obj, files = {}) {
    let result = null;

    Object.entries(obj).forEach(([key, val]) => {
      let asStr;
      let theType = this.TYPES[typeof val];
      let fileName = null;
      if (val == null) {
        asStr = '';
        theType = this.TYPES.none;
      } else if (val instanceof Date) {
        asStr = val.toISOString();
        theType = this.TYPES.date;
      } else if (Array.isArray(val)) {
        theType = this.TYPES.array;
        asStr = this.serialize(val, files[key]);
      } else if (val instanceof Buffer) {
        const isFile = Object.keys(files).includes(key);
        if (isFile) {
          fileName = files[key];
          theType = this.TYPES.file;
          asStr = val;
        } else {
          theType = this.TYPES.string;
          asStr = val.toString();
        }
      } else if (val.constructor.name === 'Object') {
        asStr = this.serialize(val, files[key]);
        theType = this.TYPES.slip;
      } else {
        switch (typeof val) {
          case 'string':
            asStr = val;
            break;
          case 'number':
            asStr = `${val}`;
            break;
          case 'boolean':
            asStr = `${+val}`;
            break;
          default:
            throw new SlipError(`Can't serialize ${val.constructor.name} ${key}`);
        }
      }

      const escKey = this.escapeCharacters(key);
      let sz;
      if ([this.TYPES.boolean, this.TYPES.none].includes(theType)) {
        sz = '';
      } else if ([this.TYPES.file, this.TYPES.slip, this.TYPES.array].includes(theType)) {
        sz = `${asStr.byteLength}|`;
      } else {
        sz = `${asStr.length}|`;
      }
      const serializedFileName = fileName ? this.escapeCharacters(fileName) : '';
      const toConcat = [Buffer.from(`${escKey}|${theType}${sz}`), Buffer.from(asStr)];
      if (result) {
        toConcat.unshift(result);
      }
      if (serializedFileName) {
        toConcat.push(Buffer.from(serializedFileName));
      }
      toConcat.push(Buffer.from(';'));
      result = Buffer.concat(toConcat)
    });

    return result;
  }

  static deserialize(buf) {
    let toParse;
    if (buf instanceof Buffer) {
      toParse = buf.toString();
    } else {
      throw new SlipError(`Expected a Buffer, got ${buf.constructor.name}`);
    }

    const result = {};
    let currentIndex = 0;

    const checkLength = () => {
      if (currentIndex > toParse.length) {
        throw new SlipError({ idx: currentIndex, str: toParse });
      }
    };

    while (currentIndex < toParse.length) {
      const rawKey = this.findBorder(toParse, currentIndex, '|');
      if (rawKey == null) {
        throw new SlipError({ str: toParse });
      }
      if (rawKey.length === 0) {
        throw new SlipError('Key can\'t be empty');
      }
      // key + |
      currentIndex += rawKey.length + 1;
      checkLength();

      const key = this.unEscapeCharacters(rawKey);
      const type = invert(this.TYPES)[toParse[currentIndex]];
      if (!type) {
        throw new SlipError({ str: toParse, idx: currentIndex });
      }
      const typeSymbol = this.TYPES[type];
      // type
      currentIndex++;
      checkLength();

      let contentSize;
      if (typeSymbol === this.TYPES.boolean) {
        contentSize = 1;
      } else if (typeSymbol === this.TYPES.none) {
        contentSize = 0;
      } else {
        const contentSizeBorder = toParse.indexOf('|', currentIndex);
        if (contentSizeBorder === -1) { // no border found
          throw new SlipError({ str: toParse });
        }
        if (contentSizeBorder === 0) { // no content size provided
          throw new SlipError(`Content size is required for type ${type}`);
        }

        const contentSizeStr = toParse.substring(currentIndex, contentSizeBorder);
        contentSize = parseInt(contentSizeStr);
        if (Number.isNaN(contentSize) || contentSizeStr.includes('.')) {
          throw new SlipError('Content size must be an Integer');
        }
        // size + |
        currentIndex += contentSizeStr.length + 1;
        checkLength();
      }

      let rawContent;
      let toAdd = contentSize;
      if ([this.TYPES.file, this.TYPES.slip, this.TYPES.array].includes(typeSymbol)) {
        rawContent = Buffer.alloc(contentSize);
        buf.copy(rawContent, 0, currentIndex, currentIndex + contentSize);
        toAdd = rawContent.toString().length;
      } else {
        rawContent = toParse.substring(currentIndex, currentIndex + contentSize);
        if (rawContent.length < contentSize) {
          throw new SlipError({ str: toParse });
        }
      }

      switch (typeSymbol) {
        case this.TYPES.date:
          const asDate = new Date(rawContent);
          // TODO: check ISO format
          if (asDate.toString() === 'Invalid Date') {
            throw new SlipError(`Could not parse Date from ${rawContent}`);
          }
          result[key] = asDate;
          break;
        case this.TYPES.string:
          result[key] = rawContent;
          break;
        case this.TYPES.number:
          result[key] = +rawContent;
          break;
        case this.TYPES.slip:
          result[key] = this.deserialize(Buffer.from(rawContent));
          break;
        case this.TYPES.file:
          result[key] = { file: rawContent };
          break;
        case this.TYPES.boolean:
          if (!['0', '1'].includes(rawContent)) {
            throw new SlipError(`Boolean must be defined as 0 or 1, got ${rawContent}`);
          }
          result[key] = !!(+rawContent);
          break;
        case this.TYPES.array:
          result[key] = [...Object.values(this.deserialize(Buffer.from(rawContent)))];
          break;
        case this.TYPES.none:
          result[key] = null;
          break;
      }

      currentIndex += toAdd;
      checkLength();
      // has file name
      if (toParse[currentIndex] !== ';') {
        const rawFileName = this.findBorder(toParse, currentIndex, ';');
        if (rawFileName == null) {
          throw new SlipError({ str: toParse });
        }
        result[key].name = this.unEscapeCharacters(rawFileName);
        // fileName
        currentIndex += rawFileName.length;
        checkLength();
      }

      // ;
      currentIndex++;
      checkLength();
    }

    return result;
  }
}

module.exports = Slip;