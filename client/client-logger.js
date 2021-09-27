const fs = require('fs');
const { formatTime } = require('../util/misc');

async function log({ type, name, state, comment }, uniqueFileSuffix) {
  const toLog = [formatTime()];

  if (type && name) {
    toLog.push(`${type}: ${name}`);
  }

  if (state) {
    if (toLog.length > 1) {
      toLog.push('  ');
    }
    toLog.push(`State: ${state}`);
  }

  if (comment) {
    if (toLog.length > 1) {
      toLog.push('  ');
    }
    toLog.push(`--> ${comment}`);
  }
  try {
    await fs.promises.mkdir('client-logs');
  } catch {}
  await fs.promises.appendFile(
    `client-logs/log_${uniqueFileSuffix}.txt`,
    `${toLog.join('')}\n`
  );
}

module.exports = log;