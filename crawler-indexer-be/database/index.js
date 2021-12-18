const mongoose = require('mongoose');
const { DB_PORT, DB_NAME, DB_HOST} = require('./settings');


async function endUsingDb() {
  return mongoose.disconnect();
}

async function startUsingDb() {
  process.on('SIGINT', endUsingDb);
  return mongoose.connect(`mongodb://${DB_HOST}:${DB_PORT}/${DB_NAME}`);
}

module.exports = { startUsingDb, endUsingDb };