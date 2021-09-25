function handle({ data: { message, attachment }, client, broadcast }) {
  broadcast({
    getData: () => ({ message, username: client.username, attachment: attachment.file }),
    files: { attachment: attachment.name }
  });
}

module.exports = handle;