const { arrIntersectionSize, toLen, flattenJSON, wAmount, get, set, getCleanIpV6, startCase, validateIpV4 } = require('../util/misc');

describe('Misc Functions', () => {
  it('arrIntersectionSize', () => {
    let arr1 = ['a', 'b', 'c', 'd'];
    let arr2 = ['c', 'd'];
    expect(arrIntersectionSize(arr1, arr2)).toBe(2);

    arr1 = [2, 3, 4, 5, 7];
    arr2 = [0, 5, 7, 4, 4, 4, 5, 7];
    expect(arrIntersectionSize(arr1, arr2)).toBe(3);

    arr1 = [-1, -2, -3, -4, -5];
    arr2 = [-1, -2, -3, -4, -5, -6];
    expect(arrIntersectionSize(arr1, arr2)).toBe(0);

    const comparator = (v1, v2) => v1.bin === v2.toString(2);
    arr1 = [{ bin: '10101' }, { bin: '11' }, { bin: '011' }, { bin: '101' }, { bin: '110' }];
    arr2 = [0, -5, 0, 3, 5, 6];
    expect(arrIntersectionSize(arr1, arr2, comparator)).toBe(2);

    arr1 = [];
    arr2 = [1, 2, 3, 4, 5];
    expect(arrIntersectionSize(arr1, arr2)).toBe(0);
    expect(arrIntersectionSize(arr2, arr1)).toBe(0);

    arr1 = ['hocus', 'pocus', 'sim', 'sim', 'salabim'];
    arr2 = ['hocus', 'pocus', 'sim', 'sim', 'salabim'];
    expect(arrIntersectionSize(arr1, arr2)).toBe(5);
  });

  it('toLen', () => {
    const num1 = 1;
    expect(toLen(num1)).toBe('01');

    const num2 = 324;
    expect(toLen(num2)).toBe('324');

    const str1 = 'col-1-header';
    expect(toLen(str1, 20, { filler: ' ' })).toBe('        col-1-header');
    expect(toLen(str1, 20, { filler: ' ', toEnd: true })).toBe('col-1-header        ');

    const str2 = 'ping';
    expect(toLen(str2, 11, { filler: 'pong', toEnd: true })).toBe('pingpongpon');

    const str3 = '-yey';
    expect(toLen(str3, 13, { filler: '-numa' })).toBe('numa-numa-yey');
  });

  it('flattenJSON', () => {
    const data = {
      name: 'Jack Sparrow',
      isCaptain: true,
      ship: {
        name: 'Black Pearl',
        expires: 13,
        stats: {
          sails: 'black',
          speedRank: 100
        }
      }
    };

    expect(flattenJSON(data)).toEqual({
      name: 'Jack Sparrow',
      isCaptain: true,
      'ship.name': 'Black Pearl',
      'ship.expires': 13,
      'ship.stats.sails': 'black',
      'ship.stats.speedRank': 100
    });
  });

  it('wAmount', () => {
    expect(wAmount(4, 'torus')).toBe('4 tori');
    expect(wAmount(8, 'Ragnaros')).toBe('8 Ragnari');
    expect(wAmount(2, 'entry')).toBe('2 entries');
    expect(wAmount(11, 'bus')).toBe('11 buses');
    expect(wAmount(7, 'ship')).toBe('7 ships');
  });

  it('get', () => {
    const obj = {
      arr: ['zero', 'one', 'two', 'three'],
      json: { user: 'Jack Sparrow', ship: 'Black Pearl' },
      ladder: { step1: { step2: { step3: 'top!' } } },
      plainString: 'Pretty Simple',
      locker: ['mummies', 'ghosts', { chest: { loot: 100 } }]
    };
    expect(get(obj, 'plainString')).toBe('Pretty Simple');
    expect(get(obj, 'json.ship')).toBe('Black Pearl');
    expect(get(obj, 'arr[2]')).toBe('two');
    expect(get(obj, 'ladder.step1.step3.step7')).toBe(null);
    expect(get(obj, ['locker', 2, 'chest'])).toEqual({ loot: 100 });
  });

  it('set', () => {
    const obj1 = {};
    set(obj1, 'field', 'Data');
    expect(obj1).toEqual({ field: 'Data' });
    set(obj1, 'extra.field', 'Extra Data');
    expect(obj1).toEqual({ field: 'Data', extra: { field: 'Extra Data' } });

    const obj2 = { arr: ['zero', 'one'] };
    set(obj2, ['arr', 4, 'hidden'], '?');
    expect(obj2).toEqual({  arr: ['zero', 'one', undefined, undefined, { hidden: '?' }] });

    expect(() => set(obj2, ['arr', '1', 'good'], 'bad')).toThrow('Can\'t set value at provided path');
  });

  it('getCleanIpV6', () => {
    expect(getCleanIpV6('::')).toBe('0:0:0:0:0:0:0:0');
    expect(getCleanIpV6('a:b:c:d::')).toBe('a:b:c:d:0:0:0:0');
    expect(getCleanIpV6('::e:f:g:h')).toBe(null);
    expect(getCleanIpV6('a:b::c:d')).toBe('a:b:0:0:0:0:c:d');
    expect(getCleanIpV6('12::a::15')).toBe(null);
    expect(getCleanIpV6('1:2:3:4:5:6:7:8')).toBe('1:2:3:4:5:6:7:8');
  });

  it('startCase', () => {
    const snakeCase = 'slithery_green_scales';
    expect(startCase(snakeCase)).toBe('Slithery green scales');
    const camelCase = 'tourToThePyramids';
    expect(startCase(camelCase)).toBe('Tour to the pyramids');
    const kebabCase = 'shwarma-shashlik-donner-kebab';
    expect(startCase(kebabCase)).toBe('Shwarma shashlik donner kebab')
  });

  it('validateIpV4', () => {
    expect(validateIpV4('8.8.8.8')).toBeTruthy();
    expect(validateIpV4('256.257.0.0')).toBeFalsy();
    expect(validateIpV4('8.88.8')).toBeFalsy();
  });
});