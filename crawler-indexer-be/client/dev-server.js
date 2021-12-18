const http = require('http');
const path = require('path');
const fs = require('fs');

const TYPE_BY_EXT = {
  '.html': 'text/html',
  '.js': 'application/javascript',
  '.css': 'text/css'
};

function runServer({ address, port }) {
  const server = http.createServer(async (request, response) => {
    const { url } = request;

    try {
      const fileName = path.join(__dirname, 'static', url === '/' ? 'index.html' : url);

      const ext = path.extname(fileName);
      const fileContent = await fs.promises.readFile(fileName);
      response.writeHead(200, { 'Content-Type': TYPE_BY_EXT[ext] });
      response.end(fileContent);
    } catch (e) {
      response.writeHead(404);
      response.end('<h1>404 not found</h1>')
    }
  });

  server.listen(port, address, () => {
    console.log(`Dev Server is running on ${address}:${port}`)
  });

  process.on('SIGINT', () => server.close());
}

module.exports = runServer;