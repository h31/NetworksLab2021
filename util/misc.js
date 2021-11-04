function toLen(str, len = 2, { filler = '0', toEnd } = {}) {
  const _str = `${str}`;
  const fillerLength = filler.length;
  const amountToAdd = len - _str.length;
  if (amountToAdd < 1) {
    return _str;
  }

  const fullFillersAmount = Math.floor(Math.abs(amountToAdd / fillerLength));
  const fullFillersCompletion = filler.repeat(fullFillersAmount);

  const fillerPartSize = amountToAdd % fillerLength;
  const fillerPart = toEnd
    ? filler.substring(0, fillerPartSize)
    : filler.substring(fillerLength - fillerPartSize);

  return toEnd
    ? `${_str}${fullFillersCompletion}${fillerPart}`
    : `${fillerPart}${fullFillersCompletion}${_str}`;
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
  while (idx1 > 0 && idx2 > 0) {
    if (comparator(arr1[--idx1], arr2[--idx2])) {
      size++;
    } else {
      break;
    }
  }
  return size;
}

function capitalize(str) {
  return `${str[0].toUpperCase()}${str.substring(1)}`;
}

function formatTime(dt) {
  const _dt = dt || new Date();
  const h = toLen(_dt.getHours());
  const m = toLen(_dt.getMinutes());
  const s = toLen(_dt.getSeconds());
  const ms = toLen(_dt.getMilliseconds(), 3);
  return `[${h}:${m}:${s}.${ms}]`;
}

function flattenJSON(json) {
  const result = {};

  const process = (_json, parentKey) => {
    Object.entries(_json).forEach(([key, value]) => {
      const fullKey = parentKey ? `${parentKey}.${key}` : key;
      if (['string', 'number', 'boolean'].includes(typeof value) || !value) {
        result[fullKey] = value;
      } else {
        process(value, fullKey);
      }
    });
  };

  process(json);

  return result;
}

/**
 *
 * @param {Array<string | number> | string} path
 * @return {Array<string | number>}
 */
function calcPath(path) {
  return Array.isArray(path) ? path : path.split(/[\]\[.]/).filter(Boolean);
}

/**
 *
 * @param {object | Array<*>} obj
 * @param {Array<string | number> | string} path
 */
function get(obj, path) {
  if (obj == null || obj.constructor.name !== 'Object' && !Array.isArray(obj)) {
    return null;
  }
  const _path = calcPath(path);
  const currKey = _path[0];
  return _path.length === 1
    ? obj[currKey]
    : get(obj[currKey], _path.slice(1))
}


/**
 *
 * @param {object | Array<*>} obj
 * @param {Array<string | number> | string} path
 * @param {*} value
 */
function set(obj, path, value) {
  const _path = calcPath(path);
  const currKey = _path[0];
  if (_path.length === 1) {
    obj[currKey] = value;
  } else {
    const atPath = obj[currKey];
    if (!atPath) {
      obj[currKey] = {};
    } else {
      const nextKeyAllowsArr = Number.isInteger(+_path[1]) && +_path[1] > 0;
      const canSet = atPath.constructor.name === 'Object' || nextKeyAllowsArr && Array.isArray(atPath);
      if (!canSet) {
        throw new Error('Can\'t set value at provided path');
      }
    }

    set(obj[currKey], _path.slice(1), value);
  }
}

function swellJSON(flatJson) {
  const result = {};

  Object.entries(flatJson).forEach(([flatKey, value]) => {
    set(result, flatKey, value);
  });

  return result;
}

/**
 *
 * @param {number} amt
 * @param {string} text
 * @return {string}
 */
function wAmount(amt, text) {
  let _text = text;
  let ending = '';
  if (amt !== 1) {
    if (text.endsWith('y')) {
      _text = text.substring(0, text.length - 1);
      ending = 'ies';
    } else if (/^[a-zA-Z]{2,}[qwrtpsdfghjklzxcvbnm][uo]s$/.test(text)) {
      ending = 'i';
      _text = text.substring(0, text.length - 2);
    } else if (text.endsWith('s')) {
      ending = 'es';
    } else {
      ending = 's'
    }
  }

  return `${amt} ${_text}${ending}`;
}

module.exports = {
  toLen,
  arrIntersectionSize,
  capitalize,
  formatTime,
  flattenJSON,
  wAmount,
  get,
  set,
  swellJSON
};