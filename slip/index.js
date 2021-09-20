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
    let result = '';
    const fileBuffers = [];
    let totalFileSize = 0;

    Object.entries(obj).forEach(([key, val]) => {
      let asStr;
      let theType = this.TYPES[typeof val];
      let extra = null;
      if (val == null) {
        asStr = '';
        theType = this.TYPES.none;
      } else if (val instanceof Date) {
        asStr = val.toISOString();
        theType = this.TYPES.date;
      } else if (Array.isArray(val)) {
        theType = this.TYPES.array;
        // TODO
      } else if (val instanceof Buffer) {
        const isFile = Object.keys(files).includes(key);
        if (isFile) {
          extra = { name: files[key], from: totalFileSize, to: totalFileSize + val.length };
          totalFileSize += val.length;
          theType = this.TYPES.file;
          fileBuffers.push(val);
          asStr = '';
        } else {
          theType = this.TYPES.string;
          asStr = val.toString();
        }
      } else if (val.constructor.name === 'Object') {
        asStr = this.serialize(val).toString();
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
      const sz = asStr.length;
      const serializedExtra = extra
        ? `|${this.escapeCharacters(extra.name)}|${extra.from}|${extra.to}`
        : '';
      result += `${escKey}|${theType}${sz}|${asStr}${serializedExtra};`;
    });

    return Buffer.concat([Buffer.from(result), ...fileBuffers]);
  }

  static deserialize(buf) {
    let toParse;
    if (buf instanceof Buffer) {
      toParse = buf.toString();
    } else if (typeof buf === 'string') {
      toParse = buf;
    } else {
      throw new SlipError(`Expected a Buffer or a String, got ${buf.constructor.name}`);
    }

    const result = {};
    let currentIndex = 0;
    let parseUntil = buf.byteLength;
    const filesToParse = [];
    while (currentIndex < parseUntil) {
      const rawKey = this.findBorder(toParse, currentIndex, '|');
      if (rawKey == null) {
        throw new SlipError('Could not find a key');
      }
      if (rawKey.length === 0) {
        throw new SlipError('Key can\'t be empty');
      }
      // key + |
      currentIndex += rawKey.length + 1;
      const key = this.unEscapeCharacters(rawKey);
      const type = invert(this.TYPES)[toParse[currentIndex]];
      if (!type) {
        throw new SlipError({ val: toParse[currentIndex], idx: currentIndex });
      }
      // type
      currentIndex++;
      const contentSizeBorder = toParse.indexOf('|', currentIndex);
      const contentSizeStr = toParse.substring(currentIndex, contentSizeBorder);
      const contentSize = parseInt(contentSizeStr);
      if (Number.isNaN(contentSize) || contentSizeStr.includes('.')) {
        throw new SlipError('')
      }
      // size + |
      currentIndex += contentSizeStr.length + 1;

      const rawContent = toParse.substring(currentIndex, currentIndex + contentSize);
      switch (this.TYPES[type]) {
        case this.TYPES.date:
          result[key] = new Date(rawContent);
          break;
        case this.TYPES.string:
          result[key] = rawContent;
          break;
        case this.TYPES.number:
          result[key] = +rawContent;
          break;
        case this.TYPES.slip:
          result[key] = this.deserialize(rawContent);
          break;
        case this.TYPES.boolean:
          result[key] = !!(+rawContent);
          break;
        case this.TYPES.array:
          // TODO
          break;
        case this.TYPES.none:
          result[key] = null;
          break;
      }

      currentIndex += contentSize;
      // has extra
      if (toParse[currentIndex] === '|') {
        currentIndex++;
        const extra = this.findBorder(toParse, currentIndex, ';');
        const rawFileName = this.findBorder(extra, 0, '|');
        const fileName = this.unEscapeCharacters(rawFileName);
        const [from, to] = extra.replace(`${rawFileName}|`, '').split('|').map(str => +str);
        parseUntil -= (to - from);
        filesToParse.push({ from, to, fileName, key });
        // extra + ;
        currentIndex += extra.length + 1;
      }
    }

    filesToParse.forEach(fileData => {
      const fileBuf = Buffer.alloc(fileData.to - fileData.from);
      buf.copy(fileBuf, 0, parseUntil + fileData.from, parseUntil + fileData.to);
      result[fileData.key] = { name: fileData.fileName, file: fileBuf };
    });

    return result;
  }
}

module.exports = Slip;