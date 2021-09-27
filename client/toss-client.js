const net = require('net');
const { useHandlers, mimeTypeIsSafe, sendChunks, useWriteQueue } = require('../util/misc');
const { SIGNALS, EVENTS, SOCKET_EVENTS, MESSAGES, LOG_TYPES } = require('../util/constants');
const TossMessenger = require('./toss-messenger');
const Pillow = require('../pillow/index');
const Slip = require('../slip/index');
const fs = require('fs');
const path = require('path');
const log = require('./client-logger');


class TossClient extends net.Socket {
  username = '';

  rl = null;
  rlLoopStarted = false;
  isTyping = false;
  bufferedMessages = [];

  logSuffix = null;

  constructor(
    { handlers = {}, handlersDir } = {},
    rl,
    options
  ) {
    super(options);
    this.rl = rl;
    this.logSuffix = (new Date()).getTime();
    useHandlers(this, {
      handlers,
      handlersDir,
      makeExtraArgs: ev => [{ client: this, ev }],
      handledEvents: SOCKET_EVENTS,
      catcherFunc: err => this.closeCompletely(err),
      log: async (ev, handler) => await log(handler ? 'handled' : 'skipped', ev, LOG_TYPES.Event, this.logSuffix)
    });
    useWriteQueue(this);

    process.on(SIGNALS.SIGINT, () => this.closeCompletely());
    rl.on(EVENTS.close, () => this.closeCompletely());
  }

  closeCompletely(error) {
    if (!this.destroyed) {
      this.destroy(error);
    }
    process.exit(error ? 1 : 0);
  }

  req(action, data, files) {
    const serializedData = Slip.serialize({ action, data }, { data: files });
    sendChunks(
      this,
      serializedData,
      chunks => Slip.serialize({ action: Pillow.actions.chunks, data: { chunks } }),
      true
    );
  }

  waitForInput(line) {
    this.isTyping = true;
    this.rl.prompt();
    this.rl.write(line);
    this.rl.once(EVENTS.line, async input => {
      await this.acceptInput(input);
    });
  }

  async askForAttachment() {
    const answer = await new Promise(resolve => this.rl.question(MESSAGES.attach, input => resolve(input)));
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

    if (fileErr) {
      TossMessenger.alertError(fileErr);
      return this.askForAttachment();
    }

    return { file, fileName };
  }

  async acceptInput(input) {
    const { file, fileName } = await this.askForAttachment();
    const toSend = { message: input };
    const files = {};
    if (file) {
      toSend.attachment = file;
      files.attachment = fileName;
    }

    this.isTyping = false;
    this.bufferedMessages.forEach(bufMsg => TossMessenger.write(bufMsg.data, bufMsg.status, bufMsg.me));
    this.bufferedMessages = [];

    this.req(Pillow.actions.sendMessage, toSend, files);
    this.rl.once(EVENTS.line, line => {
      this.waitForInput(line);
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