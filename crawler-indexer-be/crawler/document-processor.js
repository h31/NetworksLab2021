const { JSDOM } = require('jsdom');
const natural = require('natural');
const LanguageDetect = require('languagedetect');
const crypto = require('crypto');

class DocumentProcessor {
  #documents = [];
  #wordStatistics = {};

  #document = null;
  #fullText = '';

  constructor(documents = [], wordStatistics = []) {
    this.#documents = [...documents];
    this.#wordStatistics = wordStatistics.reduce((res, wordData) => ({
      ...res,
      [wordData.word]: [...wordData.tfByUrl]
    }), {});

    this.#document = null;
    this.#fullText = '';
  }

  // SHINGLES
  static #SHINGLE_SIZE = 10;
  static #DUPLICATE_THRESHOLD = 4;

  #selectShingle(shingle) {
    return Number(`0x${shingle}`) % 25 === 0;
  }

  #checksum(str, algorithm = 'md5', encoding = 'hex') {
    return crypto
      .createHash(algorithm)
      .update(str, 'utf8')
      .digest(encoding);
  }

  #makeShingles() {
    const words = this.#fullText.split(/\s+/); // No need for accurate tokenization here
    // For documents that are too short
    const shinglesAmount = Math.max(words.length - DocumentProcessor.#SHINGLE_SIZE + 1, 1);
    return [...Array(shinglesAmount)]
      .map((_, startIdx) => {
        const shingleSource = words.slice(startIdx, startIdx + DocumentProcessor.#SHINGLE_SIZE).join(' ');
        return this.#checksum(shingleSource);
      })
      .filter(s => this.#selectShingle(s));
  }

  #isDuplicate(shingles) {
    return !!this.#documents.find(
      doc => doc.shingles.filter(
        s => shingles.includes(s)
      ).length > DocumentProcessor.#DUPLICATE_THRESHOLD
    );
  }

  // STATISTICS
  #countTf(entriesCount, allWordsCount) {
    return entriesCount / allWordsCount;
  }

  #countIdf(countOfDocs, countOfAppearances, adjust = v => Math.log(v + 1)) {
    return adjust(countOfDocs / countOfAppearances);
  }

  // LINGUISTIC ANALYSIS
  static #LANG = {
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
  #langDetector = new LanguageDetect();

  #detectLang(text) {
    return this.#langDetector.detect(text, 1)[0]?.[0] || 'english';
  }

  #selectNaturalTools(...languages) {
    let config;
    for (const lang of languages) {
      config = DocumentProcessor.#LANG[lang];
      if (config != null) {
        break;
      }
    }
    if (config == null) {
      config = '';
    }

    if (typeof config === 'string') {
      return {
        tokenizer: new natural[`AggressiveTokenizer${config}`](),
        Stemmer: natural[`PorterStemmer${config}`]
      };
    }

    const { suffix, noStemmer, noPorter, noAggressive, noTokenizer } = config;
    const tokenizerName = noTokenizer
      ? 'AggressiveTokenizer'
      : `${noAggressive ? '' : 'Aggressive'}Tokenizer${suffix || ''}`;
    const stemmerName = noStemmer
      ? 'PorterStemmer'
      : `${noPorter ? '' : 'Porter'}Stemmer${suffix || ''}`;
    return { tokenizer: new natural[tokenizerName](), Stemmer: natural[stemmerName] }
  }

  #makeInverseFile(docLang, url) {
    const blocks = this.#fullText.split('\n');

    const inverseFile = {};
    let wordsInDoc = 0;
    let offset = 0;
    blocks.forEach(block => {
      const blockLang = this.#detectLang(block);
      const { Stemmer, tokenizer } = this.#selectNaturalTools(blockLang, docLang);

      const allTokens = tokenizer.tokenize(block);
      const allTokensStemmed = allTokens.map(rawToken => Stemmer.stem(rawToken).toLowerCase());

      const importantTokens = Stemmer.tokenizeAndStem(block);
      wordsInDoc += importantTokens.length;

      let tokenIdx = 0;

      importantTokens.forEach(importantToken => {
        if (!inverseFile[importantToken]) {
          inverseFile[importantToken] = [];
        }

        // Sometimes individual stemming returns result that differs from full-block stemming...
        if (allTokensStemmed.includes(importantToken)) {
          while (importantToken !== allTokensStemmed[tokenIdx]) {
            // +1 stands for a space; the offset will be estimated, but worst case the fragment will start
            // several words earlier
            offset += allTokens[tokenIdx++].length + 1;
          }
          inverseFile[importantToken].push(offset);
        } else {
          // ...when encountering such "inconsistent" word, let's consider it starts right after the previous
          // important token
          inverseFile[importantToken].push(offset + allTokens[tokenIdx].length + 1);
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
  #extractHrefs() {
    const linkElements = this.#document.getElementsByTagName('A');
    return Array.from(linkElements)
      .map(el => el.getAttribute('href'))
      .filter(href => typeof href === 'string')
      .map(href => href.trim())
      .filter(Boolean);
  }

  static #PLAIN_TEXT = '#text';
  static #SKIP_NODES = ['script', 'style', 'noscript', 'head'];
  #extractFullText() {
    const textBlocks = [this.#document.title];
    const nodes = [this.#document.body];
    let idx = 0;

    while (idx < nodes.length) {
      const currentNode = nodes[idx++];
      const { childNodes } = currentNode;

      const fullTextForBlock = [];
      const allNodesForBlock = [];
      childNodes.forEach(childNode => {
        const { textContent } = childNode;
        const nodeName = childNode.nodeName.toLowerCase();
        if (DocumentProcessor.#SKIP_NODES.includes(nodeName)) {
          return;
        }

        if (nodeName === DocumentProcessor.#PLAIN_TEXT) {
          fullTextForBlock.push(textContent);
        } else {
          allNodesForBlock.push(childNode);
        }
      });

      nodes.splice(idx, 0, ...allNodesForBlock);
      textBlocks.splice(idx, 0, fullTextForBlock.join(' '));
    }

    this.#fullText = textBlocks.join('\n');
  }

  // PROCESSING
  addDocument(contentAsString, url) {
    this.#document = new JSDOM(contentAsString).window.document;
    console.log('JSDOM done');

    this.#extractFullText();
    console.log('Text extracted');

    const shingles = this.#makeShingles();
    const duplicate = this.#isDuplicate(shingles);

    if (!duplicate) {
      const docLang = this.#detectLang(this.#fullText);
      const inverseFile = this.#makeInverseFile(docLang, url);
      console.log('Inverse file made');

      this.#documents.push({ url, shingles, lang: docLang, inverseFile, title: this.#document.title });
    } else {
      console.log('Skipping duplicate')
    }

    // Always return hrefs; catalogues are often considered duplicates, while still containing really important links
    return this.#extractHrefs();
  }

  getResult() {
    const formattedStatistics = Object.entries(this.#wordStatistics).map(([word, tfByUrl]) => ({
      word, tfByUrl, idf: this.#countIdf(this.#documents.length, Object.keys(tfByUrl).length)
    }));
    return { documents: this.#documents, wordStatistics: formattedStatistics };
  }
}

module.exports = DocumentProcessor;