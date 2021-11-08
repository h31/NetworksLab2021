const DnsReplicaServer = require('./dns-replica-server');
const InfoLogger = require('../common-classes/info-logger');
const TypedError = require('../common-classes/typed-error');


async function run({ port, database, dump, cli, address }) {
  let server;
  let existing;

  try {
    const creationResult = await DnsReplicaServer.createAccessor(database);
    server = creationResult.accessor;
    existing = creationResult.existing;

  } catch (creationErr) {
    await InfoLogger.log({
      status: InfoLogger.STATUS.error,
      comment: creationErr.message,
      occasionType: InfoLogger.OCCASION_TYPE.error,
      occasionName: creationErr instanceof TypedError ? creationErr.type : creationErr.constructor.name
    });
    process.exit(1);
  }

  try {
    if (dump) {
      await server.runDbPopulation(dump);
    }

    const noInitialData = !dump && !existing;
    if (cli || noInitialData) {
      await server.runCli(noInitialData);
    }

    server.runServer(port, address);
  } catch (workflowErr) {
    await server.closeEverything(workflowErr);
  }
}

module.exports = run;
