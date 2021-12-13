const { documentIndexSchema, wordStatisticsSchema } = require('./schemas');
const { model } = require('mongoose');

const DocumentIndex = model('DocumentIndex', documentIndexSchema);
const WordStatistics = model('WordStatistics', wordStatisticsSchema);

module.exports = { DocumentIndex, WordStatistics };