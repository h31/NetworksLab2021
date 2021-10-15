function toLen(str, len, { filler = '0', toEnd } = {}) {
  const _str = `${str}`;
  const fLen = filler.length;
  const toComplete = len - _str.length;
  if (toComplete < 1) {
    return _str;
  }

  const full = Math.abs(toComplete / fLen);
  const part = toComplete % fLen;

  if (toEnd) {
    return `${_str}${filler.substring(fLen - part)}${filler.repeat(full)}`;
  }

  return `${filler.repeat(full)}${filler.substring(0, fLen - part)}${_str}`;
}

/**
 * @template T1, T2
 * @param {Array<T1>}arr1
 * @param {Array<T2>} arr2
 * @param {(function(T1, T2): boolean)=} comparator
 * @return {number}
 */
function arrIntersectionSize(
  arr1,
  arr2,
  comparator = (v1, v2) => v1 === v2
) {
  let size = 0;
  let idx1 = arr1.length;
  let idx2 = arr2.length;
  while (idx1 * idx2 >= 0) {
    if (comparator(arr1[--idx1], arr2[--idx2])) {
      size++;
    } else {
      break;
    }
  }
  return size;
}

module.exports = {
  toLen,
  arrIntersectionSize
};