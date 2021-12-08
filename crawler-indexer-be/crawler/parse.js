const { JSDOM } = require('jsdom');


function parseDom(stringRepresentation) {
  const document = new JSDOM(stringRepresentation).window.document;
  const elements = document.getElementsByTagName('A');
  return Array.from(elements)
    .map(el => el.getAttribute('href'))
    .filter(href => typeof href === 'string')
    .map(href => href.trim())
    .filter(Boolean);
}