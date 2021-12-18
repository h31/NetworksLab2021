const normalizeUrl = require('normalize-url');

function normalize(rawUrl) {
  try {
    return normalizeUrl(rawUrl, {
      stripHash: true,
      removeQueryParameters: [/.*/],
      removeTrailingSlash: false,
      stripWWW: false
    });
  } catch {
    return null;
  }
}

// https://nodejs.org/api/url.html#url_url_resolve_from_to
function resolve(from, to) {
  const resolvedUrl = new URL(to, new URL(from, 'resolve://'));
  if (resolvedUrl.protocol === 'resolve:') {
    // `from` is a relative URL.
    const { pathname, search, hash } = resolvedUrl;
    return pathname + search + hash;
  }
  return resolvedUrl.toString();
}

module.exports = { normalize, resolve };