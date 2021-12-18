function nearestBefore(pattern, str, startIdx) {
  let idx = startIdx;
  while (!pattern.test(str[idx]) && idx > 0) {
    idx--;
  }
  return idx;
}

function nearestAfter(pattern, str, startIdx) {
  let idx = startIdx;
  while (!pattern.test(str[idx]) && idx < str.length) {
    idx++;
  }
  return idx;
}

function between(patterns, str, startIdx, size) {
  const start = nearestBefore(patterns[0], str, startIdx);
  const end = nearestAfter(patterns[1], str, start + size);
  return str.substring(start, end);
}

module.exports = {
  between
};