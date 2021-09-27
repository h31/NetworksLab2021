const { startCase, padStart } = require('lodash');
const fs = require('fs');
const childProcess = require('child_process');
const { SAFE_MIME_TYPES, EVENTS, QUEUE_STOPPER, LOG_STATES } = require('./constants');

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

function handleChunks(receiver, receivedChunk, log = () => {}) {
  let shouldContinueHandling = true;
  let dataToHandle = receivedChunk;
  if (receiver.chunksToReceive) {
    receiver.completeData = Buffer.concat([receiver.completeData || Buffer.alloc(0), receivedChunk]);
    receiver.chunksToReceive -= receivedChunk.byteLength;
    shouldContinueHandling = !receiver.chunksToReceive;
  }

  if (receiver.completeData && shouldContinueHandling) {
    dataToHandle = receiver.completeData;
    receiver.completeData = null;
  }
  log(receivedChunk.byteLength, receiver.chunksToReceive);

  return { shouldContinueHandling, dataToHandle };
}

function useWriteQueue(sock, log = () => {}) {
  sock.writeQueue = [];
  sock.isWriting = false;
  sock.canProceed = true;

  sock.writeSafe = (...toWrite) => {
    sock.writeQueue.push(...toWrite);
    log(LOG_STATES.enqueued, sock.writeQueue.length);
    sock.emit(EVENTS.queue);
  }

  const afterWrite = err => {
    log(LOG_STATES.written, sock.writeQueue.length, err);
    if (err) {
      sock.emit(EVENTS.error, err);
    } else {
      sock.isWriting = false;
      sock.emit(EVENTS.queue);
    }
  };

  sock.on(EVENTS.queue, () => {
    if (sock.writeQueue.length && !sock.isWriting && sock.canProceed) {
      const writeArgs = sock.writeQueue.splice(0, 1)[0];
      if (writeArgs[0] === QUEUE_STOPPER) {
        log(LOG_STATES.stopped, sock.writeQueue.length);
        sock.canProceed = false;
        return;
      }

      const cb = err => {
        if (writeArgs[1] && typeof writeArgs[1] === 'function') {
          writeArgs[1](err);
        }
        afterWrite(err);
      }

      sock.isWriting = true;
      const sentInOneChunk = sock.write(writeArgs[0], err => cb(err));
      if (!sentInOneChunk) {
        sock.once(EVENTS.drain, () => cb());
      }
    }
  });

  sock.on(EVENTS.proceedQueue, () => {
    log(LOG_STATES.proceeded, sock.writeQueue.length);
    sock.canProceed = true;
    sock.emit(EVENTS.queue);
  });
}

function sendChunks(
  sock, toSend, wrap, wait = false,
  { writeFunc = 'writeSafe', cb = () => {}, log = () => {} } = {}
) {
  const chunks = toSend.byteLength;
  log(chunks);
  const toWrite = [[wrap(chunks)], [toSend, cb]];
  if (wait) {
    toWrite.splice(1, 0, [QUEUE_STOPPER]);
  }
  sock[writeFunc](...toWrite);
}

module.exports = {
  capitalCamelCase,
  formatTime,
  wAmount,
  useHandlers,
  fileExists,
  mimeTypeIsSafe,
  handleChunks,
  useWriteQueue,
  sendChunks
};