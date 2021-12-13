const http = require('http');
const https = require('https');
const normalizeUrl = require('normalize-url');

const CLIENTS = { http, https };
const RESPONSE_TYPE = {
  OK: 'OK',
  REDIRECT: 'REDIRECT',
  NO_DATA: 'NO_DATA'
};
const EVENTS = {
  error: 'error',
  data: 'data',
  end: 'end'
};

// https://habr.com/ru/company/semrush/blog/441024/
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

function normalize(rawUrl) {
  return normalizeUrl(rawUrl, {
    stripHash: true,
    removeQueryParameters: [/.*/],
    removeTrailingSlash: false
  });
}

// https://nodejs.org/api/url.html#url_url_resolve_from_to
function resolve(from, to) {
  const resolvedUrl = new URL(to, new URL(from, 'resolve://'));
  if (resolvedUrl.protocol === 'resolve:') {
    // `from` is a relative URL.
    const { pathname, search, hash } = resolvedUrl;
    return pathname + search + hash;
  }
  return resolvedUrl.toString();
}

module.exports = { fetch, normalize, resolve, RESPONSE_TYPE };