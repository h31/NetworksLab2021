const GenericDnsAccessor = require('../common-classes/generic-dns-accessor');
const path = require('path');
const InfoLogger = require('../common-classes/info-logger');
const { wAmount } = require('../util/misc');
const { EVENTS } = require('../util/constants');

class DnsReplicaServer extends GenericDnsAccessor {
  static #CLI_BEHAVIOUR = {
    D: 'dump',
    C: 'cli',
    S: 'server'
  };

  static #POPULATION_BEHAVIOUR = {
    A: 'append',
    R: 'rewrite',
    C: 'cancel'
  };

  /**
   *
   * @param {number} database
   * @return {Promise<{existing: boolean, accessor: DnsReplicaServer}>}
   */
  static async createAccessor(database) {
    return super.createAccessor({
      write: console.log, coloured: true
    }, {
      database, mark: 'Server'
    });
  }

  runServer(port, address) {
    const { SOCK_EVENTS } = require('../util/constants');
    const { useHandlers } = require('../util/hooks');

    useHandlers({
      applyTo: this.sock,
      handlersDir: path.join(__dirname, 'event-handlers'),
      occasionType: InfoLogger.OCCASION_TYPE.event,
      handledEvents: SOCK_EVENTS,
      makeExtraArgs: () => [this]
    });

    this.sock.bind(port, address);
  }

  async runCli(withWarning) {
    const rl = this.runRl();

    const behaviour = withWarning ?
      await rl.insistOnAnswer(
        DnsReplicaServer.#CLI_BEHAVIOUR,
        'Warning: the database is empty, and no dump was provided.\n'[InfoLogger.STATUS.warn] +
        'Please enter one of the following:\n' +
        ' > D - enter the path to dump right away\n' +
        ' > C - enter the cli mode\n' +
        ' > S - run the server anyway without any further setup\n',
        'Please enter either D (provide a dump), C (enter cli mode) or S (run the server)  '
      )
      : DnsReplicaServer.#CLI_BEHAVIOUR.C;

    switch (behaviour) {
      case DnsReplicaServer.#CLI_BEHAVIOUR.S:
        rl.close();
        return;
      case DnsReplicaServer.#CLI_BEHAVIOUR.D:
        const dump = path.normalize(await rl.question('Enter the path to the dump:  '));
        await this.runDbPopulation(dump, rl);
        return;
      case DnsReplicaServer.#CLI_BEHAVIOUR.C:
        console.log('Type "--exit" to exit cli');
        rl.prompt();
        await new Promise(resolve => {
          rl.on(EVENTS.line, async input => {
            rl.interactive = false;
            if (input === '--exit') {
              return resolve();
            }

            const output = await this.convenientRedis.callCommand(input.split(' '));
            console.log(output);
            rl.prompt();
          });
        });
        rl.close();
        return;
    }
  }

  async runDbPopulation(dump, rl) {
    const dbSize = await this.convenientRedis.dbSize();
    let behaviour;
    if (dbSize) {
      const _rl = rl || this.runRl();
      behaviour = await _rl.insistOnAnswer(
        DnsReplicaServer.#POPULATION_BEHAVIOUR,
        `There is some data in the database already (${wAmount(dbSize, 'entry')}). ` +
        'Do you want to rewrite it [R], append the new data to it [A], or cancel [C]?  ',
        'Please enter R for rewrite, A for append or C for cancel  '
      );
      _rl.close();
    } else {
      behaviour = DnsReplicaServer.#POPULATION_BEHAVIOUR.A
    }

    if (behaviour === DnsReplicaServer.#POPULATION_BEHAVIOUR.C) {
      return;
    }

    if (behaviour === DnsReplicaServer.#POPULATION_BEHAVIOUR.R) {
      await this.convenientRedis.flushDb();
    }

    const fs = require('fs');

    await InfoLogger.log({
      status: InfoLogger.STATUS.info,
      comment: 'Populating the database, please wait...'
    });
    const rawDumpData = await fs.promises.readFile(dump);
    const jsonDumpData = JSON.parse(rawDumpData.toString());
    for (const dataEntry of jsonDumpData) {
      await this.convenientRedis.insertWithTag(dataEntry, dataEntry.name);
    }
    await this.convenientRedis.save();
    await InfoLogger.log({
      status: InfoLogger.STATUS.success,
      comment: 'Successfully populated the database with provided data'
    });
  }
}

module.exports = DnsReplicaServer;