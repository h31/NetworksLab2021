const { MESSAGES, SIGNALS, EVENTS, SOCKET_EVENTS, MESSAGE_PART } = require('./util/constants');
const readline = require('readline')
const net = require('net')
const UI = require('./ui');
const { useHandlers } = require('./util/hooks');
const path = require('path');
const Logger = require('./logger');
const Slip = require('./slip');
const Pillow = require('./pillow');
const { mimeTypeIsSafe, wAmount } = require('./util/misc');
const fs = require('fs');


class TossClient {
  #sock;
  #rl;
  username;

  currentMessagePart = MESSAGE_PART.HEADER;
  headerChunkIdx = 0;
  toCollect = 0;
  body = Buffer.alloc(0)
  isBodyCollected = false;

  isServerActive = true;
  rlLoopStarted = false;
  isTyping = false;
  bufferedMessages = [];

  writeQueue = [];
  isWriting = false;

  constructor() {
    this.#rl = readline.createInterface({
      input: process.stdin,
      output: process.stdout,
      prompt: 'TOSS >'
    });
    this.#sock = new net.Socket()
    UI.init();

    process.on(SIGNALS.SIGINT, () => this.finish());
    this.#rl.on(EVENTS.close, () => this.finish());
  }

  finish(err) {
    if (!this.#sock.destroyed) {
      this.#sock.destroy(err);
    }
  }

  pingQueue() {
    this.#sock.emit(EVENTS.pingQueue);
  }

  sockOnce(event, listener) {
    this.#sock.once(event, listener);
  }

  /**
   *
   * @param {Object=} toSend
   * @param {Object=} files
   * @return {Promise<boolean>}
   */
  async writeRaw(toSend, files) {
    const body = Slip.serialize(toSend, { data: files });
    const header = Slip.makeHeader(body);
    const fullMessage = Buffer.concat([header, body]);
    let sentInOneChunk;
    await new Promise((resolve, reject) => {
      sentInOneChunk = this.#sock.write(fullMessage, err => err ? reject(err) : resolve());
    });
    return sentInOneChunk;
  }

  async writeSafely(toSend, files) {
    this.writeQueue.push([toSend, files]);
    await Logger.log({
      comment: `${wAmount(this.writeQueue.length, 'entry')} waiting to be sent`,
      state: Logger.LOG_STATE.enqueued
    });
    this.pingQueue();
  }

  async run(port, address) {
    this.username = await this.getAnswer(MESSAGES.askUsername);
    useHandlers(this.#sock, {
      makeExtraArgs: () => [this],
      handlersDir: path.join(__dirname, 'event-handlers'),
      handledOccasions: SOCKET_EVENTS,
      occasionType: Logger.OCCASION_TYPE.event,
    });
    this.#sock.connect(port, address);
  }

  waitForInput(line) {
    this.isTyping = true;
    this.#rl.prompt();
    this.#rl.write(line);
    this.#rl.once(EVENTS.line, async input => {
      await this.acceptInput(input);
    });
  }

  /**
   *
   * @param {string} question
   * @return {Promise<string>}
   */
  async getAnswer(question) {
    return new Promise(resolve => this.#rl.question(question, answer => resolve(answer)));
  }

  async askForAttachment() {
    const answer = await this.getAnswer(MESSAGES.attach)
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
      UI.alertError(fileErr);
      return this.askForAttachment();
    }

    return { file, fileName };
  }

  async acceptInput(input) {
    const { file, fileName } = await this.askForAttachment();
    const data = { message: input };
    const files = {};
    if (file) {
      data.attachment = file;
      files.attachment = fileName;
    }

    const toSend = { action: Pillow.actions.sendMessage, data };

    this.isTyping = false;
    this.bufferedMessages.forEach(bufMsg => UI.write(bufMsg.data, bufMsg.status, bufMsg.me));
    this.bufferedMessages = [];

    await this.writeSafely(toSend, files);
    this.#rl.once(EVENTS.line, line => {
      this.waitForInput(line);
    });
  }

  startRlLoop() {
    if (!this.rlLoopStarted) {
      this.rlLoopStarted = true;
      this.#rl.once(EVENTS.line, line => this.waitForInput(line));
    }
  }

  displayMessage(data, status) {
    const me = data.username === this.username;
    if (this.isTyping) {
      this.bufferedMessages.push({ data, status, me });
    } else {
      UI.write(data, status, me);
    }
  }
}

module.exports = TossClient;