class SlipError extends Error {
  constructor(data) {
    const message = typeof data === 'string'
      ? data
      : `Unexpected character (${data.val}) at ${data.idx}`
    super(message);
  }

}

module.exports = SlipError;