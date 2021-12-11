import { Schema } from 'mongoose';

const documentIndexSchema = new Schema({
  url: String,
  lang: String,
  shingles: [Number],
  inverseFile: [{
    word: String,
    entries: [{
      blockIdx: Number,
      offset: Number
    }],
  }]
});

export {
  documentIndexSchema
};