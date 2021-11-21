const TossClient = require('./toss-client');
const yargs = require('yargs/yargs');
const { hideBin } = require('yargs/helpers')
const Logger = require('./logger');


async function run({ port, address }) {
  await Logger.init();
  const client = new TossClient();
  await client.run(port, address);
}

const argv = yargs(hideBin(process.argv))
  .options({
    address: {
      alias: 'a',
      type: 'string',
      default: 'localhost'
    },
    port: {
      alias: 'p',
      type: 'number',
      default: 8000
    }
  }).argv;

run(argv);