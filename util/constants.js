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

module.exports = {
  SIGNALS,
  EVENTS,
  SOCK_EVENTS
};