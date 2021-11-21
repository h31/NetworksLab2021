const EVENTS = {
  close: 'close',
  connection: 'connection',
  error: 'error',
  listening: 'listening',

  connect: 'connect',
  data: 'data',
  drain: 'drain',
  end: 'end',
  lookup: 'lookup',
  ready: 'ready',
  timeout: 'timeout',

  line: 'line',

  pingQueue: 'ping-queue',
  proceedQueue: 'proceed-queue',
};

const SOCKET_EVENTS = [
  EVENTS.close,
  EVENTS.connect,
  EVENTS.data,
  EVENTS.drain,
  EVENTS.end,
  EVENTS.error,
  EVENTS.lookup,
  EVENTS.ready,
  EVENTS.timeout,

  EVENTS.pingQueue,
  EVENTS.proceedQueue
];

const SIGNALS = {
  SIGINT: 'SIGINT',
};

const ERRORS = {
  ECONNREFUSED: 'ECONNREFUSED',
  ECONNRESET: 'ECONNRESET'
};

const MESSAGES = {
  askUsername: 'Please enter your username\n',
  serverError: 'Server error, please try reconnecting in a few minutes\n',
  strangeServerResponse: 'The server responded with invalid data; please make sure you are connected to the right server\n',
  unknownError: 'Unknown error\n',
  attach: 'Attach a file (or press Enter to send a message without attachments)\n'
};

const SAFE_MIME_TYPES = [
  'audio/',
  'image/',
  'text/plain', // .txt

  'application/pdf',

  'video/',

  'application/vnd.oasis.opendocument', // OpenDocument

  'application/vnd.openxmlformats-officedocument', // MS Office
  'application/msword', // Old MS Office Word
  'application/vnd.ms-excel' // Old MS Office Excel
];
const MESSAGE_PART = {
  HEADER: 'Header',
  BODY: 'Body',
};

module.exports = {
  EVENTS,
  SIGNALS,
  ERRORS,
  SOCKET_EVENTS,
  MESSAGES,
  SAFE_MIME_TYPES,
  MESSAGE_PART
};