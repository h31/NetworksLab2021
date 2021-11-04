const UI = require('./ui');
const DnsReplicaClient = require('./dns-replica-client');

async function run({ database }) {
  UI.initTheme();
  const { accessor: client } = await DnsReplicaClient.createAccessor(database);

  client.runClient();
  client.runInteractionLoop()
}

module.exports = run;