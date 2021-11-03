const InfoLogger = require('./info-logger');
const redis = require('redis');
const { SIGNALS, EVENTS } = require('../util/constants');
const dgram = require('dgram');

class GenericDnsAccessor {
  /**
   *
   * @param {Object} loggerConfig configurations for the InfoLogger
   * @param {function(string): void | Promise<void>} loggerConfig.write how to output the built message
   * @param {boolean} [loggerConfig.coloured] should colours be used
   * @param {Object} dbConfig configurations for the Redis database
   * @param {number=} dbConfig.database database number (if none provided, a new one will be created with automatic number)
   * @param {string} dbConfig.mark what the database would be used for ('server', 'client' etc.)
   * @return {Promise<{ dbClient, sock, existing }>}
   */
  static async createAccessor({ write, coloured }, { database, mark }) {
    InfoLogger.init(write, coloured);

    if (database === 0) {
      throw new Error('Database #0 is reserved');
    }

    try {
      const { databaseToConnect, existing } = await this.#requestDatabaseConnection(database, mark);
      const dbClient = redis.createClient({ database: databaseToConnect });
      await dbClient.connect();

      const sock = dgram.createSocket('udp4');

      const dbType = existing ? 'existing' : 'new';
      await InfoLogger.log({
        status: InfoLogger.STATUS.info,
        comment: `Connected to ${dbType} Redis database #${databaseToConnect}`
      });


      const closeEverything = async () => {
        await dbClient.save();
        await dbClient.quit();
        await InfoLogger.log({
          status: InfoLogger.STATUS.info,
          comment: 'Saved a dump of the Redis database and disconnected from it'
        });

        sock.close();
      }

      process.on(SIGNALS.SIGINT, closeEverything);
      dbClient.on(EVENTS.error, closeEverything);
      sock.on(EVENTS.error, closeEverything);

      return { dbClient, sock, existing };
    } catch (e) {
      await InfoLogger.log({
        status: InfoLogger.STATUS.error,
        occasionType: InfoLogger.OCCASION_TYPE.error,
        occasionName: e.constructor.name,
        comment: e.message
      });
      throw e;
    }
  }

  static async #requestDatabaseConnection(requestedDatabase, mark) {
    const requester = redis.createClient({ database: 0 });
    await requester.connect();

    const allMarks = await requester.sMembers('marks');
    const usedDatabases = [];
    for (const m of allMarks) {
      const withSuchMark = (await requester.sMembers(`usedDatabases:${m}`));
      usedDatabases.push(...withSuchMark.map(dbNumber => ({ mark: m, dbNumber: +dbNumber })));
    }

    let databaseToConnect;
    let existing = false;

    if (requestedDatabase) {
      const withSuchNumber = usedDatabases.find(ud => ud.dbNumber === requestedDatabase);
      if (withSuchNumber) {
        if (withSuchNumber.mark !== mark) {
          await requester.quit();
          throw new Error(`Attempt to use an existing database #${requestedDatabase} marked "${withSuchNumber.mark}" as "${mark}"`);
        }

        existing = true;
      }

      databaseToConnect = requestedDatabase;
    } else {
      databaseToConnect = 1;
      while (usedDatabases.find(ud => ud.dbNumber === databaseToConnect)) {
        databaseToConnect++;
      }
    }

    await requester.sAdd(`usedDatabases:${mark}`, `${databaseToConnect}`);
    await requester.sAdd('marks', mark);
    await requester.save();
    await requester.quit();

    return { databaseToConnect, existing };
  }
}

module.exports = GenericDnsAccessor;