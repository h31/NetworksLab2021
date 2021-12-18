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
      alias: 'm',
      default: 50,
      desc: 'Maximum depth of hrefs'
    },
    'total-pages': {
      type: 'number',
      alias: 't',
      desc: 'Maximum total pages to crawl; no limit if not specified'
    },
    'deep-first': {
      type: 'boolean',
      alias: 'd',
      desc: 'Perform deep-first search (broad-first by default)',
      default: false
    }
  })
  .check(argv => {
    const { maxDepth, entryPoint, totalPages } = argv;
    validatePosInt(maxDepth, 'maxDepth')
    validatePosInt(totalPages, 'totalPages');

    const url = new URL(entryPoint);
    if (!['http:', 'https:'].includes(url.protocol)) {
      return `Unsupported protocol: ${url.protocol.replace(':', '')}`;
    }

    return true;
  })
  .argv;

crawl(crawlArgs);