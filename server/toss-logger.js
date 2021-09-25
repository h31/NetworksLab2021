const colors = require('colors');
const { formatTime } = require('../util/misc');

class TossLogger {
  static status = {
    success: 'success',
    error: 'error',
    info: 'info',
    warn: 'warn',
    plain: 'plain',
    prefix: 'prefix',
  };
  static initServerTheme() {
    colors.setTheme({
      [this.status.success]: 'brightGreen',
      [this.status.error]: 'red',
      [this.status.info]: 'blue',
      [this.status.warn]: 'brightYellow',
      [this.status.plain]: 'white',
      [this.status.prefix]: 'cyan'
    });
  }

  static log({ type, name, status, state, comment }) {
    const loggingStatus = Object.values(this.status).includes(status) ? status : this.status.plain;

    const toLog = [`${formatTime()} `[this.status.plain]];

    if (type && name) {
      toLog.push(
        `${type}: `[this.status.plain],
        `${name}`[this.status.info]
      );
    }

    if (state) {
      if (toLog.length > 1) {
        toLog.push('  ');
      }
      toLog.push(
        `State: `[this.status.plain],
        `${state}`[loggingStatus]
      );
    }

    if (comment) {
      if (toLog.length > 1) {
        toLog.push('  ');
      }
      toLog.push(
        `--> `[this.status.plain],
        `${comment}`[loggingStatus]
      );
    }

    console.log(...toLog);
  }
}

module.exports = TossLogger;