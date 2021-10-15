const { arrIntersectionSize } = require('../util/misc');

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
  });
});