const { startCase, padStart } = require('lodash');
const fs = require('fs');
const childProcess = require('child_process');
const { SAFE_MIME_TYPES, EVENTS, QUEUE_STOPPER, LOG_STATES, LOG_TYPES, LOG_NAMES } = require('./constants');

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

function logReceivedChunks(receivedChunks, chunksToReceive, logger) {
  const ending = chunksToReceive
    ? `${wAmount(chunksToReceive, 'byte')} to go`
    : 'nothing more to receive';
  const comment = `Received ${wAmount(receivedChunks, 'byte')}, ${ending}`;
  logger({
    type: LOG_TYPES.Chunks,
    name: LOG_NAMES.chunksReceived,
    comment,
    status: chunksToReceive ? 'prefix' : 'success',
    state: chunksToReceive ? LOG_STATES.collectingChunks : LOG_STATES.doneChunks
  });
}

function logSentChunks(chunks, logger) {
  logger({
    type: LOG_TYPES.Chunks,
    name: LOG_NAMES.chunksSent,
    status: 'prefix',
    comment: `Enqueued ${wAmount(chunks, 'byte')} of data to be sent`
  });
}

function logWriteQueue(state, queueSize, err, logger) {
  const waiting = `${queueSize ? wAmount(queueSize, 'item') : 'No items'} waiting to be sent`;
  const comment = err
    ? `${err.constructor.name}: ${err.message}`
    : waiting;
  let status;
  if (err) {
    status = 'error';
  } else {
    switch (state) {
      case LOG_STATES.stopped:
        status = 'warn';
        break;
      case LOG_STATES.proceeded:
      case LOG_STATES.enqueued:
        status = 'prefix';
        break;
      case LOG_STATES.written:
        status = 'success';
        break;
    }
  }
  logger({ state: err ? LOG_STATES.error : state, comment, status });
}

function handleChunks(receiver, receivedChunk) {
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
  logReceivedChunks(receivedChunk.byteLength, receiver.chunksToReceive, receiver.logger);

  return { shouldContinueHandling, dataToHandle };
}

function useWriteQueue(sock) {
  sock.writeQueue = [];
  sock.isWriting = false;
  sock.canProceed = true;

  sock.writeSafe = (...toWrite) => {
    sock.writeQueue.push(...toWrite);
    logWriteQueue(LOG_STATES.enqueued, sock.writeQueue.length, null, sock.logger);
    sock.emit(EVENTS.queue);
  }

  const afterWrite = err => {
    logWriteQueue(LOG_STATES.written, sock.writeQueue.length, err, sock.logger);
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
        logWriteQueue(LOG_STATES.stopped, sock.writeQueue.length, null, sock.logger);
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
    logWriteQueue(LOG_STATES.proceeded, sock.writeQueue.length, null, sock.logger);
    sock.canProceed = true;
    sock.emit(EVENTS.queue);
  });
}

function sendChunks(
  sock, toSend, wrap, wait = false,
  { writeFunc = 'writeSafe', cb = () => {} } = {}
) {
  const chunks = toSend.byteLength;
  logSentChunks(chunks, sock.logger);
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