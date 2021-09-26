const net = require('net');
const { useHandlers, mimeTypeIsSafe } = require('../util/misc');
const { SIGNALS, EVENTS, SOCKET_EVENTS, MESSAGES } = require('../util/constants');
const TossMessenger = require('./toss-messenger');
const Pillow = require('../pillow/index');
const Slip = require('../slip/index');
const fs = require('fs');
const path = require('path');


class TossClient extends net.Socket {
  rl = null;
  username = '';
  rlLoopStarted = false;
  isTyping = false;
  bufferedMessages = [];

  constructor(
    { handlers = {}, handlersDir } = {},
    rl,
    options
  ) {
    super(options);
    this.rl = rl;
    useHandlers(this, {
      handlers,
      handlersDir,
      makeExtraArgs: ev => [{ client: this, ev }],
      handledEvents: SOCKET_EVENTS,
      catcherFunc: err => this.closeCompletely(err)
    });

    process.on(SIGNALS.SIGINT, () => this.closeCompletely());
    rl.on(EVENTS.close, () => this.closeCompletely());
  }

  closeCompletely(error) {
    if (!this.destroyed) {
      this.destroy(error);
    }
    process.exit(error ? 1 : 0);
  }

  req(action, data, files, cb) {
    const serializedData = Slip.serialize({ action, data }, { data: files });
    return this.write(serializedData, cb);
  }

  waitForInput(line) {
    this.isTyping = true;
    this.rl.prompt();
    this.rl.write(line);
    this.rl.once(EVENTS.line, input => {
      this.acceptInput(input);
    });
  }

  acceptInput(input) {
    this.rl.question(MESSAGES.attach, async answer => {
      let file;
      let fileName;
      let fileErr = null;
      if (answer) {
        try {
          file = await fs.promises.readFile(answer, { encoding: null });
          const safe = await mimeTypeIsSafe(answer);
          if (!safe) {
            fileErr = 'Files of this type are not allowed to send';
          }
          fileName = path.basename(answer);
        } catch {
          fileErr = `Failed to read file at ${answer}`;
        }
      }

      this.isTyping = false;
      this.bufferedMessages.forEach(bufMsg => TossMessenger.write(bufMsg.data, bufMsg.status, bufMsg.me));
      this.bufferedMessages = [];

      if (!fileErr) {
        const toSend = { message: input };
        const files = {};
        if (file) {
          toSend.attachment = file;
          files.attachment = fileName;
        }
        this.req(Pillow.actions.sendMessage, toSend, files);
      } else {
        TossMessenger.alertError(fileErr);
      }

      this.rl.once(EVENTS.line, line => {
        this.waitForInput(line);
      });
    });
  }

  startRlLoop() {
    if (!this.rlLoopStarted) {
      this.rlLoopStarted = true;
      this.rl.once(EVENTS.line, line => this.waitForInput(line));
    }
  }

  displayMessage(data, status) {
    const me = data.username === this.username;
    if (this.isTyping) {
      this.bufferedMessages.push({ data, status, me });
    } else {
      TossMessenger.write(data, status, me);
    }
  }

  _destroy(error, cb) {
    return super._destroy(error, (_error) => {
      if (!_error) {
        TossMessenger.alertChatBot(`Goodbye${this.username ? `, ${this.username}` : ''}!`);
      } else {
        this.emit(EVENTS.error, _error);
      }
      if (cb) {
        cb(_error);
      }
      process.exit(_error ? 1: 0)
    });
  }
}

module.exports = TossClient;