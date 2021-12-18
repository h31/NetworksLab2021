const LanguageDetect = require('languagedetect');
const natural = require('natural');


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
const langDetector = new LanguageDetect();

function detectLang(text) {
  return langDetector.detect(text, 1)[0]?.[0] || 'english';
}

function selectNaturalTools(...languages) {
  let config;
  for (const lang of languages) {
    config = LANG[lang];
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

module.exports = { detectLang, selectNaturalTools };