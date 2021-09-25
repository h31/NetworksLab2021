const TossError = require('../toss-error');


function handle({ data: { username }, client, server, broadcast }) {
  const duplicateUsername = !!server.clients.find(c => c.username === username);
  const noUsername = !username;
  if (duplicateUsername || noUsername) {
    const commentEnding = noUsername ? 'no username' : `duplicate username: ${username}`;
    const errText = noUsername ? 'Username can\'t be empty' : `User with username ${username} already exists`;
    throw new TossError({ username: [errText] }, `Attempt to connect with ${commentEnding}`);
  }

  client.username = username;
  broadcast({
    getData: c => c.username === username ? { } : { username }
  });
}

module.exports = handle;