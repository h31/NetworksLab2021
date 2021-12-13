const { fetch, normalize, resolve, RESPONSE_TYPE } = require('./requests');
const { startUsingDb, endUsingDb } = require('../database');
const { DocumentIndex, WordStatistics } = require('../database/models');
const robotsParser = require('robots-txt-parser');
const DocumentProcessor = require('./document-processor');


async function crawl({ entryPoint: rawEntryPoint, maxDepth, maxPages }) {
  await startUsingDb();

  const robots = robotsParser({ userAgent: 'GreenBeaverBot' });

  const storedDocuments = await DocumentIndex.find({}).exec();
  const storedUrls = storedDocuments.map(doc => doc.url);

  const storedStatistics = await WordStatistics.find({}).exec();

  const documentProcessor = new DocumentProcessor(storedDocuments, storedStatistics);

  const entryPoint = normalize(rawEntryPoint);
  const toCrawl = [{ href: entryPoint, depth: 0 }];

  let idx = 0;
  let exploredPages = 0;
  while (idx < toCrawl.length && (maxPages == null || exploredPages <= maxPages)) {
    const { href: currentHref, depth: currentDepth } = toCrawl[idx++];
    let canCrawl = false;
    try {
      canCrawl = await robots.canCrawl(currentHref);
    } catch (e) {
      console.log(`Error while processing robots.txt for ${currentHref}:`, e);
      // Better skip if robots.txt can't be parsed / fetched
    }
    if (!canCrawl) {
      console.log(`Crawling restricted for ${currentHref}`);
      continue;
    }
    if (currentDepth <= maxDepth) {
      try {
        const fetchResult = await fetch(currentHref);

        let hrefs = [];
        console.log(`${currentHref} --> code: ${fetchResult.code}, type: ${fetchResult.type}`);
        switch (fetchResult.type) {
          case RESPONSE_TYPE.OK:
            hrefs = documentProcessor.addDocument(fetchResult.content, currentHref);
            exploredPages++;
            break;
          case RESPONSE_TYPE.REDIRECT:
            hrefs = [fetchResult.location];
            break;
        }

        const accurateHrefs = hrefs.map(href => {
          try {
            return normalize(resolve(currentHref, href))
          } catch (e) {
            console.log(`Could not normalize ${href}:`, e);
            return null;
          }
        }).filter(Boolean);
        const uniqueHrefs = Array.from(new Set(accurateHrefs));
        const unusedHrefs = uniqueHrefs.filter(
          href => !toCrawl.find(c => c.href === href) && !storedUrls.includes(href)
        );
        toCrawl.splice(idx, 0, ...unusedHrefs.map(href => ({ href, depth: currentDepth + 1 })));

      } catch (e) {
        console.log(`Error while fetching ${currentHref}:`, e);
      }
    }
  }

  const { wordStatistics, documents } = documentProcessor.getResult();
  console.log(`Crawling done, ${exploredPages} pages explored, saving data...`);
  await DocumentIndex.create(documents);
  console.log('Saved documents');
  for (const stat of wordStatistics) {
    const savedVersion = await WordStatistics.findOne({ word: stat.word }).exec();
    if (savedVersion) {
      const upd = { ...stat };
      delete upd.word;
      await WordStatistics.updateOne({ word: stat.word }, upd).exec();
    } else {
      await WordStatistics.create(stat);
    }
  }
  console.log('Saved word statistics');

  await endUsingDb();
  console.log('Disconnected from MongoDB');
}

module.exports = crawl;