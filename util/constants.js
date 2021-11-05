const SIGNALS = {
  SIGINT: 'SIGINT',
};

const EVENTS = {
  close: 'close',
  connect: 'connect',
  error: 'error',
  listening: 'listening',
  message: 'message',

  line: 'line',
};

const SOCK_EVENTS = [
  EVENTS.close,
  EVENTS.error,
  EVENTS.message,
  EVENTS.listening,
  EVENTS.connect
];

const OPCODE = {
  standardQuery: 0,
  inverseQuery: 1,
  status: 2
};

module.exports = {
  SIGNALS,
  EVENTS,
  SOCK_EVENTS,
  OPCODE
};