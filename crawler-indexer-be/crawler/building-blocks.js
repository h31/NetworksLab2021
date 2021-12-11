import * as http from 'http';
import * as https from 'https';
import { JSDOM } from 'jsdom';
import normalizeUrl from 'normalize-url';
import natural from 'natural';
import LanguageDetect from 'languagedetect';


const langDetector = new LanguageDetect();
const LANG = {
  english: '',
  farsi: 'Fa',
  french: 'Fr',
  russian: 'Ru',
  spanish: 'Es',
  italian: 'It',
  polish: { suffix: 'Pl', noStemmer: true },
  portuguese: 'Pt',
  norwegian: 'No',
  swedish: 'Sv',
  vietnamese: { suffix: 'Vi', noStemmer: true },
  indonesian: { suffix: 'Id', noPorter: true },
  japanese: { suffix: 'Ja', noAggressive: true, noPorter: true },
  dutch: { suffix: 'Nl', noTokenizer: true }
};

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
    removeQueryParameters: true
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

function extractHrefs(document) {
  const linkElements = document.getElementsByTagName('A');
  return Array.from(linkElements)
    .map(el => el.getAttribute('href'))
    .filter(href => typeof href === 'string')
    .map(href => href.trim())
    .filter(Boolean);
}

function detectLang(text) {
  return langDetector.detect(text, 1)[0]?.[0] || 'english';
}

function selectNaturalTools(lang) {
  const config = LANG[lang] || '';

  if (typeof config === 'string') {
    return {
      tokenizer: new natural[`AggressiveTokenizer${config}`](),
      stemmer: natural[`PorterStemmer${config}`]
    };
  }

  const { suffix, noStemmer, noPorter, noAggressive, noTokenizer } = config;
  const tokenizerName = noTokenizer
    ? 'AggressiveTokenizer'
    : `${noAggressive ? '' : 'Aggressive'}Tokenizer${suffix || ''}`;
  const stemmerName = noStemmer
    ? 'PorterStemmer'
    : `${noPorter ? '' : 'Porter'}Stemmer${suffix || ''}`;
  return { tokenizer: new natural[tokenizerName](), stemmer: natural[stemmerName] }
}

function makeInverseFile(fullText) {
  const paragraphs = fullText.split('\n');
  return paragraphs.map(par => {
    const lang = detectLang(par);
    const naturalTools = selectNaturalTools(lang);
    return naturalTools.tokenizer.tokenize(par).map(token => naturalTools.stemmer.stem(token));
  });
}

function processDocument(contentAsString) {
  const document = new JSDOM(contentAsString).window.document;
  const fullText = document.body.textContent;
  const lang = detectLang(fullText);
  const inverseFile = makeInverseFile(fullText);

  const hrefs = extractHrefs(document);
  return { hrefs, data: { lang, inverseFile } };
}

export { fetch, processDocument, normalize, resolve, RESPONSE_TYPE };