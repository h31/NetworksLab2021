const { formatTime } = require('../util/misc');

class InfoLogger {
  static #coloured = false;
  static #initialized = false;

  static async write() {
    throw new Error('Not implemented');
  }

  static STATUS = {
    error: 'error',
    info: 'info',
    warn: 'warn',
    success: 'success',
    plain: 'plain',
  };

  static OCCASION_TYPE = {
    event: 'Event',
    error: 'Error'
  };

  static LOG_STATE = {
    handled: 'Handled',
    failed: 'Failed',
    skipped: 'Skipped'
  };

  /**
   *
   * @param {function(string): void | Promise<void>} write
   * @param {boolean=} coloured
   */
  static init(write, coloured) {
    if (this.#initialized) {
      throw new Error('Attempt to reinitialize the InfoLogger');
    }
    this.write = write;
    this.#initialized = true;
    this.#coloured = coloured;
    if (coloured) {
      const colors = require('colors');
      colors.setTheme({
        [InfoLogger.STATUS.info]: 'brightBlue',
        [InfoLogger.STATUS.error]: 'red',
        [InfoLogger.STATUS.warn]: 'yellow',
        [InfoLogger.STATUS.success]: 'brightGreen',
        [InfoLogger.STATUS.plain]: 'white'
      });
    }
  }

  static #wrap(text, status) {
    return this.#coloured ? text[status] : text;
  }

  static async log({ status, comment, state, occasionType, occasionName }) {
    const logStatus = Object.keys(InfoLogger.STATUS).includes(status)
      ? status
      : InfoLogger.STATUS.plain;

    const toWrite = [formatTime()];

    if (occasionType && occasionName) {
      toWrite.push(`  ${occasionType}: ${occasionName}`);
    }

    if (state) {
      toWrite.push(`  State: `, this.#wrap(state, logStatus));
    }

    if (comment) {
      toWrite.push('  -->  ', this.#wrap(comment, logStatus));
    }

    await this.write(toWrite.join(''));
  }
}

module.exports = InfoLogger;