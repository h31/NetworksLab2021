const Logger = require('../logger');


function useHandlers(
  applyTo, {
    makeExtraArgs = () => [],
    handlersDir = '',
    extractOne = null,
    defaultHandler = null,
    catcherFunc = null,
    handledOccasions = [],
    occasionType = null
  } = {}
) {
  const getHandler = occasionName => {
    let fromDir;
    try {
      fromDir = require(`${handlersDir}/${occasionName}`);
    } catch {
    }
    return fromDir || defaultHandler;
  };

  const executeHandler = async (occasionName, handler, ...args) => {
    if (handler) {
      const allArgs = [...args, ...makeExtraArgs(occasionName)];

      try {
        await handler(...allArgs);
        await Logger.log({
          occasionName,
          occasionType,
          state: Logger.LOG_STATE.handled
        });
      } catch (e) {
        await Logger.log({
          occasionName,
          occasionType,
          state: Logger.LOG_STATE.failed,
          comment: e.message
        });
        if (catcherFunc) {
          await catcherFunc(e, occasionName);
        } else {
          throw e;
        }
      }
    } else {
      await Logger.log({
        occasionName,
        occasionType,
        state: Logger.LOG_STATE.skipped
      });
    }
  };


  if (extractOne != null) {
    return () => executeHandler(extractOne, getHandler(extractOne));
  } else {
    handledOccasions.forEach(occasionName => {
      applyTo.on(occasionName, async (...args) => {
        await executeHandler(occasionName, getHandler(occasionName), ...args);
      });
    });
  }
}

module.exports = { useHandlers };