const yargs = require('yargs/yargs');
const { hideBin } = require('yargs/helpers');
const crawl = require('./crawl');


function validatePosInt(num, name) {
  if (num < 0 || !Number.isInteger(num)) {
    throw new Error(`${name} must be a positive integer`);
  }
}

const crawlArgs = yargs(hideBin(process.argv))
  .options({
    'entry-point': {
      type: 'string',
      alias: 'e',
      demandOption: true,
      desc: 'URL to start crawling at'
    },
    'max-depth': {
      type: 'number',
      alias: 'd',
      default: 50,
      desc: 'Maximum depth of hrefs'
    },
    'max-pages': {
      type: 'number',
      alias: 'p',
      desc: 'Maximum total pages to crawl; no limit if not specified'
    }
  })
  .check(argv => {
    const { maxDepth, entryPoint, maxPages } = argv;
    validatePosInt(maxDepth, 'maxDepth')
    validatePosInt(maxPages, 'maxPages');

    const url = new URL(entryPoint);
    if (!['http:', 'https:'].includes(url.protocol)) {
      return `Unsupported protocol: ${url.protocol.replace(':', '')}`;
    }

    return true;
  })
  .argv;

crawl(crawlArgs);