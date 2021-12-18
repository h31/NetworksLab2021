const http = require('http');
const { DocumentIndex, WordStatistics } = require('../database/models');
const { detectLang, selectNaturalTools } = require('../ling-utils');
const { startUsingDb, endUsingDb } = require('../database');
const { between } = require('./util');


const STATUS_CODES = {
  notImplemented: 501,
  notFound: 404,
  ok: 200
};

const METHOD = 'GET';
const ENDPOINT_PATTERN = /\/api\/search\?q=.+/;
const TO_OMIT = '/api/search?q=';
const PREVIEW_SIZE = 45;

function runServer({ address, port }) {
  const server = http.createServer(async (request, response) => {
    const { method, url } = request;
    console.log(`${method} ${url}`);
    response.setHeader('Access-Control-Allow-Origin', '*');
    response.setHeader('Access-Control-Allow-Methods', 'OPTIONS, GET');
    response.setHeader('Access-Control-Max-Age', 2592000); // 30 days

    if (method !== METHOD) {
      response.writeHead(STATUS_CODES.notImplemented);
      return response.end();
    }

    if (!ENDPOINT_PATTERN.test(url)) {
      response.writeHead(STATUS_CODES.notFound);
      return response.end();
    }

    const query = decodeURI(url.replace(TO_OMIT, ''));

    const queryLang = detectLang(query);
    const { Stemmer } = selectNaturalTools(queryLang);
    const importantTokens = Stemmer.tokenizeAndStem(query);
    if (!importantTokens.length) {
      importantTokens.push(query);
    }

    const searchPattern = new RegExp(importantTokens.map(token => `(.*${token}.*)`).join('|'));
    // statistics for all words matching the request
    const words = await WordStatistics.find({ word: searchPattern, lang: queryLang }).exec();

    const results = {};
    for (const wordData of words) {
      const { tfByUrl, idf, word } = wordData;

      // iterate through docs that contain this word
      for (const tfData of tfByUrl) {
        const { url, tf } = tfData;
        const tfIdf = tf * idf;
        if (results[url]) {
          results[url].tfIdf += tfIdf;
          results[url].needPreview.push(word);
        } else {
          const docData = await DocumentIndex.findOne({ url }).exec();
          const { title, text, inverseFile } = docData;

          results[url] = { tfIdf, title, inverseFile, text, needPreview: [word] };
        }
      }
    }

    const sortedResults = Object.entries(results)
      .sort(([, dataA], [, dataB]) => dataB.tfIdf - dataA.tfIdf)
      .map(([url, data]) => {
        const preview = [];
        const { needPreview, text, title, inverseFile } = data;
        needPreview.forEach(word => {
          const entries = inverseFile.get(word);

          entries.forEach(entryOffset => {
            const previewBlock = between([/\s/, /\s/], text, entryOffset, PREVIEW_SIZE);
            if (previewBlock.includes(word)) {
              // in case smth went wrong when indexing
              preview.push(previewBlock);
            }
          });
        });

        // in case smth went REALLY wrong when indexing
        if (!preview.length) {
          preview.push(between([/\s/, /\s/], text, title.length + 2, PREVIEW_SIZE));
        }

        return { url, title, preview };
      });

    response.writeHead(STATUS_CODES.ok, { 'Content-Type': 'application/json' });
    response.end(JSON.stringify(sortedResults));
  });

  server.listen(port, address,async () => {
    await startUsingDb();
    console.log(`Server is running on ${address}:${port}`)
  });

  server.on('close', async e => {
    await endUsingDb();
    console.log(`Server was closed${e ? ' due to an error' : ''}`)
  })

  process.on('SIGINT', () => server.close());
}

module.exports = runServer;