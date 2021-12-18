const puppeteer = require('puppeteer');
const { normalize, resolve } = require('./util');
const { detectLang, selectNaturalTools } = require('../ling-utils');

class DocumentProcessor {
  #documents = [];
  #wordStatistics = {};

  #browser = null;
  #page = null;

  #fullText = '';

  constructor(documents = [], wordStatistics = []) {
    this.#documents = [...documents];
    this.#wordStatistics = wordStatistics.reduce((res, wordData) => ({
      ...res,
      [wordData.word]: [...wordData.tfByUrl]
    }), {});

    this.#fullText = '';
  }

  // BROWSER
  async launch() {
    this.#browser = await puppeteer.launch();
    this.#page = await this.#browser.newPage();
    console.log('Launched a browser and a new page');
  }

  async stop() {
    await this.#page.close();
    await this.#browser.close();
    this.#browser = null;
    this.#page = null;
  }

  #checkBrowser() {
    if (!this.#browser) {
      throw new Error('Can\'t work without a launched browser');
    }
  }

  // STATISTICS
  #countTf(entriesCount, allWordsCount) {
    return entriesCount / allWordsCount;
  }

  #countIdf(countOfDocs, countOfAppearances, adjust = v => Math.log(v + 1)) {
    return adjust(countOfDocs / countOfAppearances);
  }

  // LINGUISTIC ANALYSIS
  #makeInverseFile(docLang, url) {
    const blocks = this.#fullText.split('\n');

    const inverseFile = {};
    let wordsInDoc = 0;
    let offset = 0;
    blocks.forEach(block => {
      const blockLang = detectLang(block);
      const { Stemmer, tokenizer } = selectNaturalTools(blockLang, docLang);

      const allTokens = tokenizer.tokenize(block);
      const allTokensStemmed = allTokens.map(rawToken => Stemmer.stem(rawToken).toLowerCase());

      const importantTokens = Stemmer.tokenizeAndStem(block);

      wordsInDoc += importantTokens.length;

      let tokenIdx = 0;

      const lastToken = allTokens[allTokens.length - 1];
      const lastWordOffset = lastToken ? block.length - lastToken.length : 0;

      importantTokens.forEach(importantToken => {
        if (!inverseFile[importantToken]) {
          inverseFile[importantToken] = [];
        }

        // Sometimes individual stemming returns result that differs from full-block stemming...
        if (allTokensStemmed.includes(importantToken)) {
          while (importantToken !== allTokensStemmed[tokenIdx] && tokenIdx < allTokens.length) {
            // +1 stands for a space; the offset will be estimated, but worst case the fragment will start
            // several words earlier
            offset += allTokens[tokenIdx++].length + 1;
          }
          inverseFile[importantToken].push(importantToken === allTokensStemmed[tokenIdx] ? offset : lastWordOffset);
        } else {
          // ...when encountering such "inconsistent" word, let's consider it starts right after the previous
          // important token
          const theToken = allTokens[tokenIdx++];
          inverseFile[importantToken].push(theToken ? offset + theToken.length + 1 : lastWordOffset);
        }
      });

      // +1 stands for \n
      offset = block.length + 1;
    });

    Object.entries(inverseFile).forEach(([word, entries]) => {
      if (!this.#wordStatistics[word]) {
        this.#wordStatistics[word] = [];
      }
      this.#wordStatistics[word].push({ url, tf: this.#countTf(entries.length, wordsInDoc) });
    });
    return inverseFile;
  }

  // HTML INSPECTION
  async #extractHrefs() {
    return this.#page.$$eval('a', linkElements => linkElements
      .map(el => el.getAttribute('href'))
      .filter(href => typeof href === 'string')
      .map(href => href.trim())
      .filter(Boolean)
    );
  }

  // static #PLAIN_TEXT = '#text';
  static #SKIP_NODES = ['script', 'style', 'noscript', 'head'];
  async #extractFullText(title) {
    const textBlocks = [title];
    const body = await this.#page.$('body');
    const nodes = [body];
    let idx = 0;

    while (idx < nodes.length) {
      const currentNode = nodes[idx++];
      const childNodes = await currentNode.$$(':scope > *');
      if (!childNodes.length) {
        const textContent = await currentNode.evaluate(node => node.textContent);
        if (textContent) {
          textBlocks.splice(idx, 0, textContent);
        }
      } else {
        const importantNodes = [];
        for (const childNode of childNodes) {
          const nodeName = await childNode.evaluate(node => node.nodeName.toLowerCase());
          if (!DocumentProcessor.#SKIP_NODES.includes(nodeName)) {
            importantNodes.push(childNode);
          }
        }
        nodes.splice(idx, 0, ...importantNodes);
      }
    }

    this.#fullText = textBlocks.join('\n');
  }

  // PROCESSING
  async addDocument(url, onSuccess) {
    this.#checkBrowser();
    try {
      const response = await this.#page.goto(url, { waitUntil: ['load', 'domcontentloaded'] });
      const statusCode = response.status();
      console.log(`${statusCode} ${url}`);

      const codeGroup = Math.floor(statusCode / 100);
      if (response.ok()) {
        const title = await this.#page.title();
        await this.#extractFullText(title);
        console.log('Text extracted');

        const docLang = detectLang(this.#fullText);
        const inverseFile = this.#makeInverseFile(docLang, url);
        console.log('Inverse file made');

        this.#documents.push({ url, lang: docLang, inverseFile, title, text: this.#fullText });
        onSuccess();
        const allHrefs = await this.#extractHrefs();
        return allHrefs.map(href => normalize(resolve(url, href))).filter(Boolean);

      } else if (codeGroup === 3 && response.headers().location) {
        return [response.headers().location];
      } else {
        return [];
      }
    } catch (e) {
      console.log(`Error while fetching ${url}:`, e);
      return [];
    }
  }

  async getResult() {
    const formattedStatistics = Object.entries(this.#wordStatistics).map(([word, tfByUrl]) => ({
      word, tfByUrl, idf: this.#countIdf(this.#documents.length, Object.keys(tfByUrl).length)
    }));
    await this.stop();
    return { documents: this.#documents, wordStatistics: formattedStatistics };
  }
}

module.exports = DocumentProcessor;