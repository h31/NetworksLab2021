const http = require('http');
const https = require('https');


const CLIENTS = { http, https };
const RESPONSE_TYPE = {
  OK: 'OK', // code (2xx), content
  REDIRECT: 'REDIRECT', // code (3xx), location
  NO_DATA: 'NO_DATA' // code
};
const EVENTS = {
  error: 'error',
  data: 'data',
  end: 'end'
};

/**
 *
 * @param {string} destination
 * @return {Promise<{
 *  code: number,
 *  location: string?,
 *  content: string?,
 *  type: 'OK' | 'REDIRECT' | 'NO_DATA'
 * }>}
 */
function fetch(destination) {
  const destinationUrl = new URL(destination);
  const protocol = destinationUrl.protocol.replace(':', '');
  const client = CLIENTS[protocol];
  if (!client) {
    throw new Error(`Could not select a client for ${protocol}`);
  }

  return new Promise((resolve, reject) => {
    const request = client.get(destinationUrl.href, response => {
      const code = response.statusCode;
      const codeGroup = Math.floor(code / 100);
      if (codeGroup === 2) {
        const body = [];
        response.setEncoding('utf8');
        response.on(EVENTS.data, chunk => body.push(chunk));
        response.on(EVENTS.end, () => resolve({ code, content: body.join(''), type: RESPONSE_TYPE.OK }));
      } else if (codeGroup === 3 && response.headers.location) {
        resolve({ code, location: response.headers.location, type: RESPONSE_TYPE.REDIRECT });
      } else {
        resolve({ code, type: RESPONSE_TYPE.NO_DATA });
      }
    });
    request.on(EVENTS.error, err => reject(err));
    request.end();
  });
}

fetch.RESPONSE_TYPE = RESPONSE_TYPE;

module.exports = fetch;