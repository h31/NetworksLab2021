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
    desc: 'Start server',
    handler: require('./server'),
    builder: (_yargs) => _yargs.options({
      port: { type: 'number', default: 41234, alias: 'p', desc: 'Port to bind the server socket to' },
      dump: { type: 'string', normalize: true, alias: 'd', desc: 'A .json file with the data to fill the database' },
      cli: { type: 'boolean', default: false, alias: 'c', desc: 'Start in cli mode' },
      address: { type: 'string', default: 'localhost', alias: 'a', desc: 'Address to bind the server socket to' }
    })
  })
  .command({
    command: COMMANDS.client,
    desc: 'Start client',
    handler: require('./client')
  })
  .demandCommand(1, 1, DEMAND_COMMAND_MSG, DEMAND_COMMAND_MSG)
  .options({
    database: { type: 'number', desc: 'Redis database to use. A new one is created if nothing provided. #0 is reserved', alias: 'n' },
  })
  .parse();