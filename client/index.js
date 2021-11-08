const UI = require('./ui');
const DnsReplicaClient = require('./dns-replica-client');
const InfoLogger = require('../common-classes/info-logger');
const TypedError = require('../common-classes/typed-error');

async function run({ database }) {
  UI.initTheme();
  let client;
  try {
    const creationResult = await DnsReplicaClient.createAccessor(database);
    client = creationResult.accessor;
  } catch (creationErr) {
    await InfoLogger.log({
      status: InfoLogger.STATUS.error,
      comment: creationErr.message,
      occasionType: InfoLogger.OCCASION_TYPE.error,
      occasionName: creationErr instanceof TypedError ? creationErr.type : creationErr.constructor.name
    });
    UI.displayError(`Failed running the resolver:\n  ${creationErr.message}`);
    process.exit(1);
  }

  try {
    client.runClient();
    client.runInteractionLoop()
  } catch (workflowErr) {
    UI.displayError(`Error while handling your request:\n  ${workflowErr.message}`);
    await client.closeEverything(workflowErr);
  }
}

module.exports = run;