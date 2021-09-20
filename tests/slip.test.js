const Slip = require('../slip/index');

const originalString = 'key|with|sticks>and>>arrows>and se;micolons';
const originalData = {
  '>strange->key': 12.43,
  data: { idx: 12, val: null, when: new Date() },
  message: 'Hello!',
  isRead: false,
  '|shed|': '|-|-|-|-|-|-|'
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
  }
);