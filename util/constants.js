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
  EVENTS.timeout
];

const SIGNALS = {
  SIGINT: 'SIGINT',
};

const ERRORS = {
  ECONNREFUSED: 'ECONNREFUSED',
  ECONNRESET: 'ECONNRESET'
};

const LOG_STATES = {
  skipped: 'Skipped',
  passedToHandle: 'Passed to handle',
  error: 'Error'
};

const LOG_TYPES = {
  Action: 'Action',
  Event: 'Event',
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

  'application/vnd.oasis.opendocument', // OpenDocument

  'application/vnd.openxmlformats-officedocument', // MS Office
  'application/msword', // Old MS Office Word
  'application/vnd.ms-excel' // Old MS Office Excel
];

module.exports = {
  EVENTS,
  SIGNALS,
  ERRORS,
  LOG_STATES,
  LOG_TYPES,
  SOCKET_EVENTS,
  MESSAGES,
  SAFE_MIME_TYPES
};