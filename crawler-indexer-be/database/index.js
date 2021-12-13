const mongoose = require('mongoose');
const { DB_PORT, DB_NAME, DB_HOST} = require('./settings');


async function startUsingDb() {
  return mongoose.connect(`mongodb://${DB_HOST}:${DB_PORT}/${DB_NAME}`);
}

async function endUsingDb() {
  return mongoose.disconnect();
}

module.exports = { startUsingDb, endUsingDb };