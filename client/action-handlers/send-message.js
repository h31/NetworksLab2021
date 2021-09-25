const fs = require('fs');
const path = require('path');
const { fileExists } = require('../../util/misc');

async function handle({ data, client, status }) {
  if (data.attachment) {
    try {
      await fs.promises.mkdir('attachments');
    } catch {}
    const ext = path.extname(data.attachment.name);
    const baseName = path.basename(data.attachment.name, ext);
    let fileName = path.join('attachments', data.attachment.name);
    let fileNo = 1;
    while (true) {
      const exists = await fileExists(fileName);
      if (!exists) {
        break;
      }
      fileName = path.join('attachments', `${baseName}(${fileNo++})${ext}`)
    }
    data.attachment.name = path.basename(fileName);
    fs.writeFile(fileName, data.attachment.file, () => {});
  }

  client.displayMessage(data, status);
}

module.exports = handle;