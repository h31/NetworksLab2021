const colors = require('colors');
const { capitalCamelCase, formatTime } = require('./util/misc');
const { capitalize } = require('lodash');
const Pillow = require('./pillow');


class UI {
  static #TYPES = {
    chatBot: 'chatBot',
    user: 'user',
    error: 'error',
    me: 'me',
  };
  static #THEME = {
    [this.#TYPES.chatBot]: 'cyan',
    [this.#TYPES.user]: 'yellow',
    [this.#TYPES.error]: 'red',
    [this.#TYPES.me]: 'green'
  };

  static init() {
    colors.setTheme(this.#THEME);
    Object.values(this.#TYPES).forEach(type => {
      UI[`alert${capitalCamelCase(type)}`] = text => console.log(text[type]);
    });
  }

  static #hdr(text, type) {
    return text[`bright${capitalize(this.#THEME[type])}`]
  }

  static write(data, statusCode, me) {
    if (Pillow.isError(statusCode)) {
      const allErrors = Object.entries(data.errors)
        .map(([field, msg]) => `  - ${field}: ${msg.join('; ')}`)
        .join('\n');
      console.log(
        this.#hdr('Error:\n', this.#TYPES.error),
        allErrors[this.#TYPES.error]
      );
      return;
    }

    if (data) {
      const isFromUser = !!data.username;
      const username = data.username || 'ChatBot';
      let type;
      if (me) {
        type = this.#TYPES.me;
      } else {
        type = isFromUser ? this.#TYPES.user : this.#TYPES.chatBot;
      }
      const message = data.attachment
        ? `${data.message || ''} [att. ${data.attachment.name}]`
        : data.message;
      console.log(
        this.#hdr(`${formatTime(data.time)} ${username}:\n`, type),
        message[type]
      );
    }
  }
}

module.exports = UI;