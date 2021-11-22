const fs = require('fs');
const path = require('path');
const { formatTime } = require('./util/misc')

class Logger {
  static #writeTo = null;
  static #ready = false;

  static LOG_STATE = {
    handled: 'Handled',
    skipped: 'Skipped',
    failed: 'Failed',

    enqueued: 'Entry Enqueued',
    written: 'Entry Written',

    proceeded: 'Queue Proceeded'
  };

  static OCCASION_TYPE = {
    event: 'Event',
    action: 'Action',
    error: 'Error',
    peekEvent: 'SlipPeek'
  };

  static async init() {
    const uniqueDateSuffix = (new Date()).getTime();
    this.#writeTo = path.join(__dirname, 'logs', `${uniqueDateSuffix}.txt`);
    try {
      await fs.promises.mkdir(path.join(__dirname, 'logs'));
    } catch {}
    this.#ready = true;
  }

  static async log({
    occasionName,
    occasionType,
    comment,
    state
  }) {
    if (!this.#ready) {
      throw new Error('No setup done for Logger');
    }

    const toWrite = [formatTime()];

    if (occasionType && occasionName) {
      toWrite.push(`  ${occasionType}: ${occasionName}`);
    }

    if (state) {
      toWrite.push(`  State: ${state}`);
    }

    if (comment) {
      toWrite.push(`  -->  ${comment}`);
    }

    await fs.promises.appendFile(this.#writeTo, `${toWrite.join('')}\n`);
  }
}

module.exports = Logger;