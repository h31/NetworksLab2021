const UI = require('./ui')
const readline = require('readline');
const _yargs = require('yargs/yargs');

class DnsReplicaClient {
  rl;
  yargs;

  async constructor(database) {
    this.yargs = _yargs()
      .options({
        questions: { alias: 'q', array: true, demandOption: true },
        full: { alias: 'f', boolean: true, default: true, description: 'Display full response' }
      })
      .fail((msg, _, yargsObj) => {
        UI.displayError(msg);
        yargsObj.help();
      });

    this.rl = readline.createInterface({
      input: process.stdin,
      output: process.stdout,
      prompt: ''
    });
  }
}

module.exports = DnsReplicaClient;