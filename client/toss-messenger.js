const colors = require('colors');
const { formatTime, capitalCamelCase } = require('../util/misc');
const { capitalize } = require('lodash');
const Pillow = require('../pillow/index');


class TossMessenger {
  static rl = null;
  static types = {
    chatBot: 'chatBot',
    user: 'user',
    error: 'error',
    me: 'me',
  };
  static theme = {
    [this.types.chatBot]: 'cyan',
    [this.types.user]: 'yellow',
    [this.types.error]: 'red',
    [this.types.me]: 'green'
  };

  static initClientTheme() {
    colors.setTheme(this.theme);
    Object.values(this.types).forEach(type => {
      TossMessenger[`alert${capitalCamelCase(type)}`] = text => console.log(text[type]);
    });
  }

  static hdr(text, type) {
    return text[`bright${capitalize(this.theme[type])}`]
  }

  static write(data, statusCode, me) {
    if (Pillow.isError(statusCode)) {
      const allErrors = Object.entries(data.errors)
        .map(([field, msg]) => `  - ${field}: ${msg.join('; ')}`)
        .join('\n');
      console.log(
        this.hdr('Error:\n', this.types.error),
        allErrors[this.types.error]
      );
      return;
    }

    if (data) {
      const isFromUser = !!data.username;
      const username = data.username || 'ChatBot';
      let type;
      if (me) {
        type = this.types.me;
      } else {
        type = isFromUser ? this.types.user : this.types.chatBot;
      }
      const message = data.attachment
        ? `${data.message || ''} [att. ${data.attachment.name}]`
        : data.message;
      console.log(
        this.hdr(`${formatTime(data.time)} ${username}:\n`, type),
        message[type]
      );
    }
  }
}

module.exports = TossMessenger;