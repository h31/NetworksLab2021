const Pillow = require('../../pillow/index');


function handle({ client }) {
  client.req(Pillow.actions.logIn, { username: client.username });
}

module.exports = handle;