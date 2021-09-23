const Slip = require('../slip/index');
const fs = require('fs');
const path = require('path');

const originalString = 'key|with|sticks>and>>arrows>and se;micolons';
const originalData = {
  '>strange->key': 12.43,
  data: { idx: 12, val: null, when: new Date() },
  // To ensure strange encodings don't mess everything up
  message: 'Привет! Ô',
  isRead: false,
  '|shed|': '|-|-|-|-|-|-|',
  yaharr: [1, 2, { obj: 'with-one-field' }]
};

describe(
  'Slip', () => {
    it('Should escape characters properly', () => {
      const escapedString = Slip.escapeCharacters(originalString);
      expect(escapedString).toBe('key>|with>|sticks>>and>>>>arrows>>and se>;micolons');
    });

    it('Should un-escape characters properly', () => {
      const escapedString = Slip.escapeCharacters(originalString);
      const unEscapedString = Slip.unEscapeCharacters(escapedString);
      expect(unEscapedString).toBe(originalString);
    });

    it('Should find the first un-escaped border properly', () => {
      const originalKey = '|key-made-by-s>>ome-weirdo';
      const infoAfterKey = '||>|gone wild with these!|||>||;<>|';
      const serializedData = `${Slip.escapeCharacters(originalKey)}|${infoAfterKey}`;
      const foundKey = Slip.findBorder(serializedData, 0, '|');
      const parsedKey = Slip.unEscapeCharacters(foundKey);
      expect(parsedKey).toBe(originalKey);
    });

    it('Should serialize data properly', () => {
      const payload = {
        str: 'A String',
        num: 12,
        none: null,
        emptyObj: {  },
        encodedStr: 'Привет! Ô'
      };
      const serializedData = Slip.serialize(payload);
      expect(serializedData.toString()).toBe(
        'str|l8|A String;num|n2|12;none|x;emptyObj|s0|;encodedStr|l16|Привет! Ô;');
    });

    it('Should deserialize data properly', () => {
      const serializedData = Slip.serialize(originalData);
      const deserializedData = Slip.deserialize(serializedData);
      expect(deserializedData).toEqual(originalData);
    });

    it('Should not deserialize invalid data, should throw errors with proper messages', () => {
      const emptyKey = 'firstKey|l4|wooo;|b1';
      expect(() => Slip.deserialize(Buffer.from(emptyKey))).toThrow(
        'Key can\'t be empty; could only parse until token at position 17'
      );

      const invalidBool = 'isGood|b1;isBad|b8;';
      expect(() => Slip.deserialize(Buffer.from(invalidBool))).toThrow(
        'Boolean must be defined as 0 or 1, got 8'
      );

      const fileNameWithoutFile = 'str|l5|Hello;num|n1|5the-file-no-one-needs.txt;';
      expect(() => Slip.deserialize(Buffer.from(fileNameWithoutFile))).toThrow(
        'num is defined as number, but has a file name'
      );

      const nonIsoDate = 'who|l2|me;dob|d10|2000-07-17;';
      expect(() => Slip.deserialize(Buffer.from(nonIsoDate))).toThrow(
        'Dates must be in ISO format'
      );

      const invalidDate = 'suggestedBy|l12|Münchhausen;whatSuggested|d24|1979-05-32T12:00:00.000Z';
      expect(() => Slip.deserialize(Buffer.from(invalidDate))).toThrow(
        'Could not parse Date from 1979-05-32T12:00:00.000Z'
      );
    });

    it('Should read data with attached text files', done => {
      fs.mkdir(path.join(__dirname, 'parsed-files'), () => {
        const pathToFile = path.join(__dirname, 'original-files', 'doc.txt');
        fs.readFile(pathToFile, { encoding: null }, (err, fData) => {
          if (err) {
            done(err);
          }

          const payload = { isImportant: true, comments: null, doc: fData };
          const serializedData = Slip.serialize(payload, { doc: 'doc.txt' });
          const deserializedData = Slip.deserialize(serializedData);
          expect(deserializedData.doc.file.equals(fData)).toBeTruthy();
          const pathToParsedFile = path.join(__dirname, 'parsed-files', deserializedData.doc.name);
          fs.writeFile(pathToParsedFile, deserializedData.doc.file, () => done());
        });
      });
    });

    it('Should read data with attached image files', done => {
      fs.mkdir(path.join(__dirname, 'parsed-files'), () => {
        const pathToFile = path.join(__dirname, 'original-files', 'good.jpg');
        fs.readFile(pathToFile, { encoding: null }, (err, fData) => {
          if (err) {
            done(err);
          }

          const payload = {
            user: 'Barash',
            message: 'Беее...',
            profileData: { created: new Date(2002, 11, 20), avatar: fData },
          };
          const serializedData = Slip.serialize(payload, { profileData: { avatar: 'good.jpg' } });
          const deserializedData = Slip.deserialize(serializedData);
          expect(deserializedData.profileData.avatar.file.equals(fData)).toBeTruthy();
          const pathToParsedFile = path.join(__dirname, 'parsed-files', deserializedData.profileData.avatar.name);
          fs.writeFile(pathToParsedFile, deserializedData.profileData.avatar.file, () => done());
        });
      });
    });
  }
);