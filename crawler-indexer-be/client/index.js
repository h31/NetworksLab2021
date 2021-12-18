const yargs = require('yargs/yargs');
const { hideBin } = require('yargs/helpers');
const runServer = require('./dev-server');

const serverArgs = yargs(hideBin(process.argv)).options({
  address: {
    alias: 'a',
    default: 'localhost',
    type: 'string'
  },
  port: {
    alias: 'p',
    default: 8000,
    type: 'number'
  }
}).argv;

runServer(serverArgs);