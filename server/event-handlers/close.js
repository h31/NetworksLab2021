const Pillow = require('../../pillow/index');


function handle(_, { client, server }) {
  server.unregisterClient(client);
  // The logged-out client is already unregistered, no need to filter
  server.broadcast(Pillow.actions.logOut, { getData: () => ({ username: client.username }) });
}

module.exports = handle;
