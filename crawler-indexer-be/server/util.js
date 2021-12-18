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

module.exports = {
  nearestBefore,
  nearestAfter
};