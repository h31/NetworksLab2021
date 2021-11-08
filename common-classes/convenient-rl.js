const readline = require('readline');
const { EVENTS } = require('../util/constants');


class ConvenientRl {
  #rl;
  interactive = true;

  constructor() {
    this.#rl = readline.createInterface({
      input: process.stdin,
      output: process.stdout,
      prompt: '> '
    });
  }

  on(event, listener) {
    const _listener = event === EVENTS.line
      ? (...args) => {
        if (this.interactive) {
          listener(...args);
        }
      }
      : listener;
    this.#rl.on(event, _listener);
  }

  /**
   *
   * @param {string} query
   * @return {Promise<string>}
   */
  question(query) {
    return new Promise(resolve => {
      this.#rl.question(query, answer => resolve(answer));
    });
  }

  prompt() {
    this.interactive = true;
    this.#rl.prompt();
  }

  close() {
    this.#rl.close();
  }

  async insistOnAnswer(answersMapping, firstTimeText, text) {
    let firstTime = true;

    const ask = async () => new Promise((resolve, reject) => {
      const q = firstTime ? firstTimeText : text;
      this.#rl.question(q, answer => {
        answersMapping[answer] ? resolve(answersMapping[answer]) : reject();
      });
    });

    while (true) {
      try {
        return ask();
      } catch {
        firstTime = false;
      }
    }
  }
}

module.exports = ConvenientRl;