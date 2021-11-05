const { flattenJSON, swellJSON } = require('../util/misc');
const TypedError = require('./typed-error');
const redis = require('redis');
const InfoLogger = require('./info-logger');

class ConvenientRedis {
  #redisClient;
  #database;
  #mark;

  constructor(database, mark) {
    if (database === 0) {
      throw new TypedError('Database #0 is reserved', TypedError.TYPE.redis);
    }

    this.#mark = mark;
    this.#database = database;
  }

  async #requestDatabaseConnection() {
    const requester = redis.createClient({ database: 0 });
    await requester.connect();

    const allMarks = await requester.sMembers('marks');
    const usedDatabases = [];
    for (const mark of allMarks) {
      const withSuchMark = await requester.sMembers(`usedDatabases:${mark}`);
      usedDatabases.push(...withSuchMark.map(dbNumber => ({ mark, dbNumber: +dbNumber })));
    }

    let databaseToConnect;
    let existing = false;

    if (this.#database) {
      const withSuchNumber = usedDatabases.find(ud => ud.dbNumber === this.#database);
      if (withSuchNumber) {
        if (withSuchNumber.mark !== this.#mark) {
          await requester.quit();
          throw new TypedError(
            `Attempt to use an existing database #${this.#database} marked "${withSuchNumber.mark}" as "${this.#mark}"`,
            TypedError.TYPE.redis
          );
        }

        existing = true;
      }

      databaseToConnect = this.#database;
    } else {
      databaseToConnect = 1;
      while (usedDatabases.find(ud => ud.dbNumber === databaseToConnect)) {
        databaseToConnect++;
      }
    }

    await requester.sAdd(`usedDatabases:${this.#mark}`, `${databaseToConnect}`);
    await requester.sAdd('marks', this.#mark);
    await requester.save();
    await requester.quit();

    return { databaseToConnect, existing };
  }

  async init() {
    const { existing, databaseToConnect } = await this.#requestDatabaseConnection();

    this.#redisClient = redis.createClient({ database: databaseToConnect });
    await this.#redisClient.connect();

    const dbType = existing ? 'existing' : 'new';
    await InfoLogger.log({
      status: InfoLogger.STATUS.info,
      comment: `Connected to ${dbType} Redis database #${databaseToConnect}`
    });

    return existing;
  }

  /**
   *
   * @param {Array<string>} args
   * @return {Promise<*>}
   */
  async callCommand(args) {
    const _args = [...args];
    if (_args[1] === '*') {
      _args[1] = await this.generateId();
    }

    return this.#redisClient.callCommand(_args);
  }

  async save() {
    await this.#redisClient.save();
  }

  async endSession() {
    await this.save();
    await this.#redisClient.quit();
    await InfoLogger.log({
      status: InfoLogger.STATUS.info,
      comment: 'Saved a dump of the Redis database and disconnected from it'
    });
  }

  async generateId() {
    const id = await this.#redisClient.incr('next-id-seq');
    return `id:${id}`;
  }

  async insertJSON(key, jsonData) {
    const keyValuePairs = Object.entries(flattenJSON(jsonData)).flat(1).map(item => `${item}`);
    return this.#redisClient.sendCommand(['HMSET', key, ...keyValuePairs]);
  }

  async retrieveJSON(key) {
    const redisHashData = await this.#redisClient.hGetAll(key);
    return redisHashData ? swellJSON(redisHashData) : null;
  }

  async getAllByTag(tag) {
    const keysForTag = await this.#redisClient.sMembers(tag);
    const valuesWithTag = [];
    for (const key of keysForTag) {
      const value = await this.retrieveJSON(key);
      if (value) {
        valuesWithTag.push(value);
      }
    }

    if (keysForTag.length && !valuesWithTag.length) {
      // all cached values have expired
      await this.#redisClient.sPop(tag, `${keysForTag.length}`);
    }

    return valuesWithTag;
  }

  async insertWithTag(data, tag, ttl) {
    const id = await this.generateId();
    await this.sAdd(tag, id);
    await this.insertJSON(id, data);
    if (ttl != null) {
      this.#redisClient.expire(id, `${ttl}`);
    }
  }

  /**
   *
   * @param {function(object): boolean | void} processEntry
   * @return {Promise<void>}
   */
  async scanRecords(processEntry) {
    let cursor = 0;
    do {
      const [nextCursor, keys] = await this.#redisClient.sendCommand(['scan', `${cursor}`, 'match', 'id:*']);
      cursor = +nextCursor;
      for (const key of keys) {
        const data = await this.retrieveJSON(key);
        if (processEntry(data)) {
          cursor = 0; // break the outer loop as well without extra variables
          break;
        }
      }
    } while (cursor);
  }

  async dbSize() {
    return this.#redisClient.dbSize();
  }

  async flushDb() {
    return this.#redisClient.flushDb();
  }

  async sAdd(...args) {
    return this.#redisClient.sAdd(...args);
  }

  on(event, listener) {
    return this.#redisClient.on(event, listener);
  }
}

module.exports = ConvenientRedis;