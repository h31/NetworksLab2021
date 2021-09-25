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
  timeout: 'timeout'
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

const LOG_STATES = {
  skipped: 'Skipped',
  passedToHandle: 'Passed to handle',
  error: 'Error'
};

const LOG_TYPES = {
  Action: 'Action',
  Event: 'Event',
};

module.exports = {
  EVENTS,
  SIGNALS,
  LOG_STATES,
  LOG_TYPES,
  SOCKET_EVENTS
};