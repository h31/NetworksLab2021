import { fetch, processDocument, normalize, resolve, RESPONSE_TYPE } from './building-blocks.js';
// import useDb from '../database';
import robotsParser from 'robots-txt-parser';

async function crawl({ entryPoint: rawEntryPoint, maxDepth }) {
  //await useDb();
  const robots = robotsParser();

  const entryPoint = normalize(rawEntryPoint);
  const toCrawl = [{ href: entryPoint, depth: 0 }];
  let idx = 0;
  while (idx < toCrawl.length) {
    const { href: currentHref, depth: currentDepth } = toCrawl[idx++];
    const canCrawl = await robots.canCrawl(currentHref);
    if (!canCrawl) {
      continue;
    }

    try {
      const fetchResult = await fetch(currentHref);

      if (currentDepth <= maxDepth) {
        const hrefs = [];
        switch (fetchResult.type) {
          case RESPONSE_TYPE.OK:
            const documentData = processDocument(fetchResult.content);
            console.log({
              url: currentHref,
              data: documentData.data
            });
            hrefs.push(...documentData.hrefs);
            break;
          case RESPONSE_TYPE.REDIRECT:
            hrefs.push(fetchResult.location);
            break;
        }

        const uniqueHrefs = Array.from(new Set(hrefs.map(href => normalize(resolve(currentHref, href)))));
        const unusedHrefs = uniqueHrefs.filter(href => !toCrawl.find(c => c.href === href));
        toCrawl.splice(idx, 0, ...unusedHrefs.map(href => ({ href, depth: currentDepth + 1 })));
      }
    } catch (e) {
      console.log(`Error while fetching ${currentHref}:`, e);
    }
  }
}

export default crawl;