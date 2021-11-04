function setupRl() {
  const readline = require('readline');

  return readline.createInterface({
    input: process.stdin,
    output: process.stdout,
    prompt: '> '
  });
}

async function insistOnAnswer(answersMapping, firstTimeText, text, rl) {
  let firstTime = true;
  const _rl = rl || setupRl();

  const ask = async () => new Promise((resolve, reject) => {
    const q = firstTime ? firstTimeText : text;
    _rl.question(q, answer => {
      answersMapping[answer] ? resolve(answersMapping[answer]) : reject();
    });
  });

  while (true) {
    try {
      return ask();
    } catch {
      firstTime = false;
    }
  }
}

module.exports = {
  setupRl,
  insistOnAnswer
};