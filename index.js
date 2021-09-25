const yargs = require('yargs/yargs');
const { hideBin } = require('yargs/helpers');


const COMMANDS = {
  server: 'server',
  client: 'client'
};
const DEMAND_COMMAND_MSG = 'You have to specify one and only one command to run';

yargs(hideBin(process.argv))
  .command({
    command: COMMANDS.server,
    desc: 'Run the tossing server',
    handler: require('./server/index')
  })
  .command({
    command: COMMANDS.client,
    desc: 'Start a tossing client',
    handler: require('./client/index')
  })
  .demandCommand(1, 1, DEMAND_COMMAND_MSG, DEMAND_COMMAND_MSG)
  .options({
    host: { default: 'localhost', type: 'string' },
    port: { default: 8000, type: 'number' }
  })
  .parse();