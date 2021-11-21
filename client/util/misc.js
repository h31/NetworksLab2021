const { startCase, padStart } = require('lodash');
const fs = require('fs');
const path = require('path');
const childProcess = require('child_process');
const { SAFE_MIME_TYPES } = require('./constants');

function capitalCamelCase(str) {
  return startCase(str).replace(/ /g, '');
}

function toLenStr(str, len = 2, filler = '0') {
  return padStart(`${str}`, len, filler);
}

function formatTime(dt) {
  const _dt = dt || new Date();
  const h = toLenStr(_dt.getHours());
  const m = toLenStr(_dt.getMinutes());
  const s = toLenStr(_dt.getSeconds());
  const ms = toLenStr(_dt.getMilliseconds(), 3);
  return `[${h}:${m}:${s}.${ms}]`;
}

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

function fileExists(path) {
  return new Promise(resolve => {
    fs.access(path, fs.constants.F_OK, err => { resolve(!err) });
  });
}

async function getMimeType(path) {
  const exists = await fileExists(path);
  if (!exists) {
    return null;
  }

  const isOnWindows = process.platform === 'win32';
  const args = [`file --mime-type -b "${path}"`];
  if (isOnWindows) {
    args.push({ shell: 'sh', windowsHide: true });
  }
  return childProcess.execSync(...args).toString();
}

async function mimeTypeIsSafe(path) {
  const mimeType = await getMimeType(path);
  return !!mimeType && !!SAFE_MIME_TYPES.find(prefix => mimeType.startsWith(prefix));
}

/**
 *
 * @param {string} dirname
 * @param {number=} [lvl = 1]
 * @return {string}
 */
function getParentDir(dirname, lvl = 1) {
  const dirsList = dirname.split(path.sep);
  return dirsList.slice(0, dirsList.length - lvl).join(path.sep);
}

module.exports = {
  capitalCamelCase,
  formatTime,
  wAmount,
  fileExists,
  mimeTypeIsSafe,
  getParentDir
};