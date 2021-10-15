const BitBuffer = require('../bit-buffer/index');

describe('BitBuffer', () => {
  it('Should make BitBuffer from a number', () => {
    const fromNumber = new BitBuffer(4);
    expect(fromNumber.data).toEqual([1, 0, 0]);

    const sized = new BitBuffer(17, { size: 16 });
    expect(sized.data).toEqual([
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      1, 0, 0, 0, 1
    ]);
  });

  it('Should make BitBuffer from a string', () => {
    const fromString = new BitBuffer('ABCD');
    expect(fromString.data).toEqual([
      0, 1, 0, 0, 0, 0, 0, 1,
      0, 1, 0, 0, 0, 0, 1, 0,
      0, 1, 0, 0, 0, 0, 1, 1,
      0, 1, 0, 0, 0, 1, 0, 0
    ]);
  });

  it('Should make BitBuffer from a Buffer', () => {
    const buf = Buffer.from([200, 1, 30, 2]);
    const fromBuf = new BitBuffer(buf);
    expect(fromBuf.data).toEqual([
      1, 1, 0, 0, 1, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 1,
      0, 0, 0, 1, 1, 1, 1, 0,
      0, 0, 0, 0, 0, 0, 1, 0
    ]);
  });

  it('Should make BitBuffer from an Array', () => {
    const nums = [256, 15, 43];
    const fromNums = new BitBuffer(nums);
    expect(fromNums.data).toEqual([
      1, 0, 0, 0, 0, 0, 0, 0, 0,
      1, 1, 1, 1,
      1, 0, 1, 0, 1, 1
    ]);

    const strings = ['Beware', 'of', 'me'];
    const fromStrings = new BitBuffer(strings);
    expect(fromStrings.data).toEqual([
      0, 1, 0, 0, 0, 0, 1, 0,
      0, 1, 1, 0, 0, 1, 0, 1,
      0, 1, 1, 1, 0, 1, 1, 1,
      0, 1, 1, 0, 0, 0, 0, 1,
      0, 1, 1, 1, 0, 0, 1, 0,
      0, 1, 1, 0, 0, 1, 0, 1,

      0, 1, 1, 0, 1, 1, 1, 1,
      0, 1, 1, 0, 0, 1, 1, 0,

      0, 1, 1, 0, 1, 1, 0, 1,
      0, 1, 1, 0, 0, 1, 0, 1
    ]);
  });

  it('.toBuffer() should work properly', () => {
    const str = 'Hello!';
    const fromString = new BitBuffer(str);
    expect(fromString.toBuffer()).toEqual(Buffer.from(str));

    const someFlags = [0, 1, 1, 0, 1, 0, 1, 1, 1, 1, 0];
    const fromFlags = new BitBuffer(someFlags);
    expect(fromFlags.toBuffer()).toEqual(Buffer.from([3, 94]));

    const bigNumber = 512;
    const fromBigNumber = new BitBuffer(bigNumber);
    expect(fromBigNumber.toBuffer()).toEqual(Buffer.from([2, 0]));
  });

  it('.append() and .prepend() should work properly', () => {
    const buf = new BitBuffer(3, { size: 4 });
    buf.append('A');
    expect(buf.data).toEqual([
      0, 0, 1, 1,
      0, 1, 0, 0, 0, 0, 0, 1,
    ]);
    buf.prepend(7);
    expect(buf.data).toEqual([
      1, 1, 1,
      0, 0, 1, 1,
      0, 1, 0, 0, 0, 0, 0, 1,
    ]);
  });

  it('.concat() should work properly', () => {
    const first = new BitBuffer(7, { size: 8 });
    const second = new BitBuffer(3);
    const third = new BitBuffer(44);
    const concatenation = BitBuffer.concat([first, second, third]);
    expect(concatenation.data).toEqual([
      0, 0, 0, 0, 0, 1, 1, 1,
      1, 1,
      1, 0, 1, 1, 0, 0
    ]);
  });
});