const path = require('path');
const InfoLogger = require('../common-classes/info-logger');


function useHandlers({
  applyTo,
  makeExtraArgs = () => [],
  handlersDir = '',
  occasionType,
  extractOne = null,
  defaultHandler = null,
  catcherFunc = null,
  handledEvents = [],
} = {}) {
  const getHandler = occasionName => {
    let fromDir;
    try {
      fromDir = require(path.join(handlersDir, occasionName));
    } catch {}
    return fromDir || defaultHandler;
  };

  const executeHandler = async (occasionName, handler, ...args) => {
    if (handler) {
      const allArgs = [...args, ...makeExtraArgs(occasionName)];

      try {
        await handler(...allArgs);
        await InfoLogger.log({
          occasionName,
          occasionType,
          state: InfoLogger.LOG_STATE.handled,
          status: InfoLogger.STATUS.success
        });
      } catch (e) {
        await InfoLogger.log({
          occasionName,
          occasionType,
          state: InfoLogger.LOG_STATE.failed,
          status: InfoLogger.STATUS.error,
          comment: e.message
        });
        if (catcherFunc) {
          await catcherFunc(e, occasionName);
        } else {
          throw e;
        }
      }
    } else {
      await InfoLogger.log({
        occasionName,
        occasionType,
        state: InfoLogger.LOG_STATE.skipped,
        status: InfoLogger.STATUS.warn
      });
    }
  }

  if (extractOne != null) {
    return () => executeHandler(extractOne, getHandler(extractOne));
  } else {
    handledEvents.forEach(ev => {
      applyTo.on(ev, async (...args) => {
        await executeHandler(ev, getHandler(ev), ...args);
      });
    });
  }
}

module.exports = {
  useHandlers
};