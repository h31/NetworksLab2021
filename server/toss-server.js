const net = require('net');
const { useHandlers, wAmount }  = require('../util/misc');
const { SIGNALS, SOCKET_EVENTS, LOG_STATES, LOG_TYPES } = require('../util/constants');
const TossLogger = require('./toss-logger');
const Pillow = require('../pillow/index');
const Slip = require('../slip/index');


class TossServer extends net.Server {
  clients = [];
  _handlers = {};
  _handlersDir = null;
  static _defaultCId = 'tsId';
  _clientIndicator = '';

  constructor(
    { handlers = {}, handlersDir, clientIndicator } = {},
    options, connectionListener
  ) {
    super(
      options,
      client => {
        this.registerClient(client);
        if (connectionListener) {
          connectionListener(client);
        }
      }
    );

    this._handlers = handlers;
    this._handlersDir = handlersDir;
    this._clientIndicator = clientIndicator || TossServer._defaultCId;

    process.on(SIGNALS.SIGINT, () => this.close());
  }

  _getHandling() {
    const handling = this.clients.length;
    return `Handling ${wAmount(handling, 'connection')} now`;
  }

  registerClient(client) {
    const useTs = this._clientIndicator === TossServer._defaultCId;
    if (useTs) {
      client[TossServer._defaultCId] = (new Date()).getTime();
    }
    client.res = (...args) => this.res(client, ...args);
    client.err = (...args) => this.err(client, ...args);
    this.clients.push(client);
    const connectionDescription = useTs
      ? `, ${TossServer._defaultCId} = ${client[TossServer._defaultCId]}` :
      '';
    TossLogger.log({
      status: TossLogger.status.success,
      comment: `Established a new connection${connectionDescription}. ${this._getHandling()}`
    });
    useHandlers(
      client, {
        makeExtraArgs: ev => [{ client, server: this, ev }],
        handlersDir: this._handlersDir,
        handlers: this._handlers,
        handledEvents: SOCKET_EVENTS,
        log: (ev, handler) => TossLogger.log({
          type: LOG_TYPES.Event,
          name: ev,
          state: handler ? LOG_STATES.passedToHandle : LOG_STATES.skipped,
          status: handler ? TossLogger.status.prefix : TossLogger.status.warn
        }),
        // Server errors (non-classified, happen because of bad code) are handled here
        // If there's an error with the server itself, not some single connected client,
        // all the connected clients will receive ECONNRESET
        catcherFunc: (err, ev) => {
          TossLogger.log({
            type: LOG_TYPES.Event,
            name: ev,
            state: LOG_STATES.error,
            status: TossLogger.status.error,
            comment: err.message
          });
          client.write(Slip.serialize({
            status: Pillow.responseStatus.ERR_SERVER.code,
            data: { errors: { _err: ['Server error'] } }
          }));
        }
      }
    );
  }

  unregisterClient(client) {
    this.clients = this.clients.filter(c => c !== client);
    TossLogger.log({
      status: TossLogger.status.info,
      comment: `Closed connection for ${client[this._clientIndicator]}. ${this._getHandling()}`
    });
  }

  res(client, action, status, data, files, cb) {
    const toSend = { action, status };
    if (data) {
      toSend.data = { ...data, time: new Date() };
    }
    const serializedData = Slip.serialize(toSend, files);
    client.write(serializedData, cb);
  }

  err(client, action, errors, status, cb) {
    const serializedData = Slip.serialize({ action, data: { errors }, status });
    client.write(serializedData, cb);
  }

  broadcast(
    action, {
      filterClients = list => list,
      getData = () => null,
      files = {},
      getStatus = () => Pillow.responseStatus.OK.code
    } = {}
  ) {
    const writeableClients = this.clients.filter(c =>
      ['open', 'writeOnly'].includes(c.readyState) &&
      !!c[this._clientIndicator]
    );
    const filteredClients = filterClients(writeableClients);
    let amount = filteredClients.length;
    let errorCount = 0;

    TossLogger.log({
      comment: `Broadcasting to ${wAmount(amount, 'client')}...`,
      status: TossLogger.status.prefix
    });

    const checkFinish = (c, err) => {
      if (err) {
        errorCount++;
        // Don't exactly know who to acknowledge when a message did not broadcast properly,
        // so let's keep this server-side-only info
        TossLogger.log({
          status: TossLogger.status.error,
          comment: `Error sending data to ${c[this._clientIndicator]}`
        });
      }
      if (--amount === 0) {
        TossLogger.log({
          comment: `Finished broadcasting with ${wAmount(errorCount, 'error')}`,
          status: errorCount ? TossLogger.status.warn : TossLogger.status.success
        });
      }
    }

    const time = new Date();
    filteredClients.forEach(c => {
      const toSend = { action, status: getStatus(c) };
      const data = getData(c);
      if (data) {
        toSend.data = { ...data, time };
      }
      c.write(Slip.serialize(toSend, { data: files }), err => checkFinish(c, err));
    });
  }

  listen(port, hostname, backlog, listeningListener) {
    return super.listen(port, hostname, backlog, (...args) => {
      const { address, port } = this.address();
      TossLogger.log({ comment: `Started the server on ${address}:${port}`, status: TossLogger.status.info });
      if (listeningListener) {
        listeningListener(...args);
      }
    });
  }

  close(callback) {
    const waitingFor = this.clients.length;
    const { address, port } = this.address();
    if (waitingFor) {
      TossLogger.log({
        comment: `Closing the server, waiting for ${wAmount(waitingFor, 'client')} to disconnect...`,
        status: TossLogger.status.prefix
      });
    }
    return super.close(() => {
      TossLogger.log({
        comment: `Closed the server on ${address}:${port}`,
        status: TossLogger.status.info
      });
    });
  }
}

module.exports = TossServer;