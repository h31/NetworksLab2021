class SlipError extends Error {
  constructor(data) {
    let message;
    if (typeof data === 'string') {
      message = data;
    } else {
      const theIdx = data.idx == null ? data.str.length : data.idx;
      message = `Unexpected character (${data.str[theIdx] || 'end of input'}) at ${theIdx}`
    }
    super(message);
  }

}

module.exports = SlipError;