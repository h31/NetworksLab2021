const { startCase, padStart } = require('lodash');
const fs = require('fs');
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

function wAmount(amount, text) {
  return `${amount} ${text}${amount === 1 ? '' : 's'}`
}

function useHandlers(
  applyTo, {
    makeExtraArgs = () => [],
    handlers = {},
    handlersDir = '',
    log = () => {},
    extractOne = null,
    defaultHandler = null,
    catcherFunc = null,
    handledEvents = []
  } = {}
) {
  const getHandler = occasion => {
    const fromHandlers = handlers[occasion];
    let fromDir;
    try {
      fromDir = require(`${handlersDir}/${occasion}`);
    } catch {}
    return fromHandlers || fromDir || defaultHandler;
  };

  const executeHandler = (occasion, handler, ...args) => {
    log(occasion, handler);
    if (handler) {
      const allArgs = [...args, ...makeExtraArgs(occasion)];
      if (catcherFunc) {
        try {
          handler(...allArgs);
        } catch (e) {
          catcherFunc(e, occasion);
        }
      } else {
        handler(...allArgs);
      }
    }
  }

  if (extractOne != null) {
    executeHandler(extractOne, getHandler(extractOne));
  } else {
    handledEvents.forEach(ev => {
      applyTo.on(ev, (...args) => {
        executeHandler(ev, getHandler(ev), ...args);
      });
    })
  }
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

module.exports = {
  capitalCamelCase,
  formatTime,
  wAmount,
  useHandlers,
  fileExists,
  mimeTypeIsSafe
};