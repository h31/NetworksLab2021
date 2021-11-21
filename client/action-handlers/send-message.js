const fs = require('fs');
const path = require('path');
const { fileExists, getParentDir } = require('../util/misc');

/**
 *
 * @param {Object} data
 * @param {TossClient} client
 * @return {Promise<void>}
 */
async function handle({ data, client, status }) {
  if (data.attachment) {
    const attachmentsPath = path.join(getParentDir(__dirname), 'attachments');
    try {
      await fs.promises.mkdir(attachmentsPath);
    } catch {
    }
    const ext = path.extname(data.attachment.name);
    const baseName = path.basename(data.attachment.name, ext);
    let fileName = path.join(attachmentsPath, data.attachment.name);
    let fileNo = 1;
    while (true) {
      const exists = await fileExists(fileName);
      if (!exists) {
        break;
      }
      fileName = path.join(attachmentsPath, `${baseName}(${fileNo++})${ext}`);
    }
    data.attachment.name = path.basename(fileName);
    await fs.promises.writeFile(fileName, data.attachment.file);
  }

  client.displayMessage(data, status);
}

module.exports = handle;
