const yargs = require('yargs/yargs');
const { hideBin } = require('yargs/helpers');
const { fileExists, validateIpV4 } = require('./util/misc');

const COMMANDS = {
  server: 'server',
  client: 'client'
};
const DEMAND_COMMAND_MSG = 'You have to specify one and only one command to run';

yargs(hideBin(process.argv))
  .command(
    COMMANDS.server,
    'Start server',
    {
      dump: {
        type: 'string',
        alias: 'd',
        normalize: true,
        desc: 'A .json file with the data to fill the database'
      },
      cli: {
        type: 'boolean',
        default: false,
        alias: 'c',
        desc: 'Start in cli mode'
      },
      port: {
        type: 'number',
        default: 41234,
        alias: 'p',
        desc: 'Port to bind the server socket to'
      },
      address: {
        type: 'string',
        default: 'localhost',
        alias: 'a',
        desc: 'Address to bind the server socket to'
      }
    },
    require('./server')
  )
  .command({
    command: COMMANDS.client,
    desc: 'Start client',
    handler: require('./client')
  })
  .demandCommand(1, 1, DEMAND_COMMAND_MSG, DEMAND_COMMAND_MSG)
  .options({
    database: {
      type: 'number',
      desc: 'Redis database to use. A new one is created if nothing provided. #0 is reserved',
      alias: 'n'
    }
  })
  .check(argv => {
    const availableOptions = ['database', '_', '$0'];
    if (argv._[0] === COMMANDS.server) {
      availableOptions.push('address', 'port', 'cli', 'dump', 'd'); // normalize option breaks the strip-alias config
    }

    const unknownOptions = Object.keys(argv).filter(providedOpt => !availableOptions.includes(providedOpt));
    if (unknownOptions.length) {
      throw new Error(`Unknown options: ${unknownOptions.join(',')}`);
    }

    if (argv.database === 0) {
      throw new Error('Database #0 is reserved for internal usage. Please select another one');
    }

    if (argv.dump && !fileExists(argv.dump)) {
      throw new Error(`File ${argv.dump} does not exist`);
    }

    if (argv._[0] === COMMANDS.server && !validateIpV4(argv.address)) {
      throw new Error(`Invalid address: ${argv.address}`);
    }

    return true;
  })
  .parserConfiguration({ 'strip-aliased': true })
  .parse();
