const fs = require('fs');
const path = require('path');

class Logger {
  #uniqueDateSuffix = '!';

  constructor() {
    this.#uniqueDateSuffix = (new Date()).getTime();
  }

  async setup() {
    try {
      await fs.promises.mkdir(path.join(__dirname, 'logs'));
    } catch {}
  }

  async log({
    occasionName,
    occasionType,
    comment,
    state
  }) {

  }
}

module.exports = Logger;