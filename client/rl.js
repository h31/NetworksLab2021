const readline = require('readline');

function initRl() {
  return readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    prompt: 'TOSS > '
  });
}

module.exports = initRl;