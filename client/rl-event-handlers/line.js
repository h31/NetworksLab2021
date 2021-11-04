const Message = require('../../common-classes/message');
const ResourceRecord = require('../../common-classes/resource-record');
const InfoLogger = require('../../common-classes/info-logger');
const UI = require('../ui');

/**
 *
 * @param {string} input
 * @param {DnsReplicaClient} client
 * @return {Promise<void>}
 */
async function handle(input, client) {
  const [parsingErr, argv, output] = await new Promise(resolve =>
    client.yargsParser.parse(input, (...res) => resolve([...res]))
  );
  if (!parsingErr) {
    const message = Message.makeRequest(client.nextRequestId++, argv.questions, {
      recDesired: argv.recursive,
      qType: ResourceRecord.TYPE[argv.type]
    });
    client.history[client.nextRequestId] = argv;
    const [sendingErr, bytes] = await new Promise(resolve =>
      client.sock.send(message, 0, message.byteLength, argv.port, argv.host, (...res) => resolve([...res]))
    );
    await InfoLogger.log({
      comment: `Sent ${bytes} bytes of data to ${argv.host}:${argv.port}`
    });
  } else {
    UI.displayError(output);
    client.rl.prompt();
  }
}

module.exports = handle;
