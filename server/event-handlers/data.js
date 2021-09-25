const { useHandlers } = require('../../util/misc');
const { LOG_STATES, LOG_TYPES } = require('../../util/constants');
const path = require('path');
const TossLogger = require('../toss-logger');
const TossError = require('../toss-error');
const Pillow = require('../../pillow/index');
const PillowError = require('../../pillow/error');
const Slip = require('../../slip/index');
const SlipError = require('../../slip/error');


function handle(rawData, { client, server, ev }) {
  let deserializedData;
  try {
    deserializedData = Slip.deserialize(rawData);
  } catch (err) {
    const isSlipError = err instanceof SlipError;
    if (!isSlipError) {
      // Will be handled by the server
      throw err;
    }
    TossLogger.log({
      type: LOG_TYPES.Event,
      name: ev,
      state: LOG_STATES.error,
      status: TossLogger.status.error,
      comment: err.message
    });
    client.write(Slip.serialize({
      status: Pillow.responseStatus.ERR_REQ_FORMAT.code,
      data: { errors: { _err: [err.message] } }
    }));
    return;
  }

  let payload;
  try {
    payload = Pillow.validateRequest(deserializedData);
  } catch (err) {
    const isPillowError = err instanceof PillowError;
    if (!isPillowError) {
      // Will be handled by the server
      throw err;
    }
    const actionToRespond = deserializedData.action;
    client.err(actionToRespond, err.errors, Pillow.responseStatus.ERR_REQ_DATA.code);
    return;
  }

  const { action, data } = payload;

  const handlersDir = path.join(
    __dirname.replace(`${path.sep}event-handlers`, ''),
    'action-handlers'
  );
  useHandlers(null, {
    makeExtraArgs: a => [{
      client, server, data, action,
      broadcast: (...args) => server.broadcast(a, ...args),
      respond: (...args) => client.res(a, ...args),
      err: (...args) => client.err(a, ...args)
    }],
    handlersDir,
    extractOne: action,
    log: (a, handler) => TossLogger.log({
      type: LOG_TYPES.Action,
      name: a,
      state: handler ? LOG_STATES.passedToHandle : LOG_STATES.skipped,
      status: handler ? TossLogger.status.prefix : TossLogger.status.warn
    }),
    catcherFunc: (err, a) => {
      const isTossError = err instanceof TossError;
      if (!isTossError) {
        // Will be handled by the server
        throw err;
      }
      TossLogger.log({
        type: LOG_TYPES.Action,
        name: a,
        state: LOG_STATES.error,
        comment: err.comment,
        status: TossLogger.status.error
      });
      client.err(a, err.errors, Pillow.responseStatus.ERR_REQ_DATA.code);
    }
  });
}

module.exports = handle;