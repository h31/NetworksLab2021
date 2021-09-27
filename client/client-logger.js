const fs = require('fs');

async function log(text, occasion, logType, uniqueFileSuffix) {
  try {
    await fs.promises.mkdir('client-logs');
  } catch {}
  await fs.promises.appendFile(
    `client-logs/log_${uniqueFileSuffix}.txt`,
    `${logType}: ${occasion} --> ${text}\n`
  );
}

module.exports = log;