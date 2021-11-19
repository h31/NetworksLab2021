class SlipError extends Error {
  constructor(data) {
    let message;
    if (!data) {
      message = 'Unexpected end of input';
    } else if (typeof data === 'string') {
      message = data;
    } else {
      message = `Unexpected token (${data.str[data.idx]}) at ${data.idx}`
    }
    super(message);
  }

}

module.exports = SlipError;