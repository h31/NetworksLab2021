const Slip = require('../slip/index');
const fs = require('fs');
const path = require('path');

const originalString = 'key|with|sticks>and>>arrows>and se;micolons';
const originalData = {
  '>strange->key': 12.43,
  data: { idx: 12, val: null, when: new Date() },
  message: 'Hello!',
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

    it('Should deserialize data properly', () => {
      const serializedData = Slip.serialize(originalData);
      const deserializedData = Slip.deserialize(serializedData);
      expect(deserializedData).toEqual(originalData);
    });

    it('Should read data with attached files', done => {
      fs.mkdir(path.join(__dirname, 'parsed-files'), { recursive: true }, () => {
        const pathToFile = path.join(__dirname, 'original-files', 'good.jpg');
        fs.readFile(pathToFile, { encoding: null }, (err, fData) => {
          if (err) {
            done(err);
          }

          const payload = { user: 'Barash', profileData: { created: new Date(2002, 11, 20), avatar: fData } };
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