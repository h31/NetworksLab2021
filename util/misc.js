const { startCase } = require('lodash');

function capitalCamelCase(str) {
  return startCase(str).replace(/ /g, '');
}

module.exports = {
  capitalCamelCase
};