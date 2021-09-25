function handle({ data: { message, attachment }, client, broadcast }) {
  const dataToSend = { message, username: client.username };
  const filesToSend = {};
  if (attachment) {
    dataToSend.attachment = attachment.file;
    filesToSend.attachment = attachment.name;
  }
  broadcast({
    getData: () => dataToSend,
    files: filesToSend
  });
}

module.exports = handle;