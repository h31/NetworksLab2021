const colors = require('colors');
const { toLen, capitalize } = require('../util/misc');


class UI {
  static status = {
    error: 'error',
    info: 'info',
    plain: 'plain',
    bright: 'bright'
  };

  static initTheme() {
    colors.setTheme({
      [this.status.error]: 'red',
      [this.status.info]: 'brightBlue',
      [this.status.plain]: 'white',
      [this.status.bright]: 'cyan'
    });

    Object.keys(this.status).forEach(s => {
      this[`display${capitalize(s)}`] = text => console.log(text[s]);
    });
  }

  /**
   *
   * @param {Array<Array<number>>} rows
   * @param {Array<number>} data
   */
  static asTable(rows, data) {
    const width = 16;
    const header = [...Array(width)].map((_, idx) => `  ${idx.toString(16)}`).join('');
    console.log(header[this.status.info]);
    const border = `+${'--+'.repeat(width)}`[this.status.plain];
    console.log(border);
    let sliceStart = 0;
    rows.forEach(row => {
      const toLog = ['|'[this.status.plain]];
      row.forEach(col => {
        const sparseStr = ` ${data.slice(sliceStart, sliceStart + col).join('  ')}`;
        toLog.push(sparseStr[this.status.bright], '|'[this.status.plain]);
        sliceStart += col;
      });
      console.log(toLog.join(''));
      console.log(border);
    });
  }

  /**
   *
   * @param {Object} data
   * @param {(function(string, Object): string)=} handleNested
   * @param {number=} pad
   */
  static asList(
    data,
    handleNested,
    pad = 0
  ) {
    const multiLevel = handleNested == null;
    const padStr = '  '.repeat(pad);

    const entries = Object.entries(data);
    const longestTitleLength = Math.max(...entries.map(e => e[0].length)) + 2;
    for (const [title, message] of entries) {
      let displayMessage = message;
      if (message && ['Array', 'Object'].includes(message.constructor.name)) {
        if (multiLevel) {
          console.log(
            padStr,
            toLen(title, longestTitleLength, { filler: ' ', toEnd: true })[this.status.plain]
          );
          this.asList(message, handleNested, pad + 1);
          continue;
        } else {
          displayMessage = handleNested(title, message);
        }
      }

      console.log(
        padStr,
        toLen(title, longestTitleLength, { filler: ' ', toEnd: true })[this.status.plain],
        `${displayMessage}`[this.status.bright]
      );
    }
  }

  static log() {

  }
}

module.exports = UI;