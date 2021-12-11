import yargs from 'yargs';
import { hideBin } from 'yargs/helpers';
import crawl from './crawl.js';


const crawlArgs = yargs(hideBin(process.argv))
  .options({
    'entry-point': {
      type: 'string',
      alias: 'e',
      demandOption: true
    },
    'max-depth': {
      type: 'number',
      alias: 'd',
      default: 50
    }
  })
  .check(argv => {
    const { maxDepth, entryPoint } = argv;
    if (maxDepth < 0 || !Number.isInteger(maxDepth)) {
      return 'maxDepth must be a positive integer';
    }

    const url = new URL(entryPoint);
    if (!['http:', 'https:'].includes(url.protocol)) {
      return `Unsupported protocol: ${url.protocol.replace(':', '')}`;
    }

    return true;
  })
  .argv;

crawl(crawlArgs);
