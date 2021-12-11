import { documentIndexSchema } from './schemas.js';
import { model } from 'mongoose';

const DocumentIndex = model('DocumentIndex', documentIndexSchema);

export {
  DocumentIndex
};