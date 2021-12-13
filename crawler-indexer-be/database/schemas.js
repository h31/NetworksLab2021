const { Schema } = require('mongoose');

const documentIndexSchema = new Schema({
  url: { type: String, index: true, unique: true },
  title: String,
  lang: String,
  shingles: [String],
  inverseFile: { // [word]: [offset]
    type: Map,
    of: [Number]
  }
});

const wordStatisticsSchema = new Schema({
  word: { type: String, index: true , unique: true },
  tfByUrl: [{ url: String, tf: Number }],
  idf: Number
});

module.exports = {
  documentIndexSchema,
  wordStatisticsSchema
};