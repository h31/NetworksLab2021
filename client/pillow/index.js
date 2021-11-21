class Pillow {
  static SUCCESS_THRESHOLD = 200;
  static responseStatus = {
    OK: 100,
    OK_EMPTY: 101,
    ERR_REQ_DATA: 200,
    ERR_REQ_FORMAT: 201,
    ERR_SERVER: 202
  };
  static actions = {
    logIn: 'log-in',
    logOut: 'log-out',
    sendMessage: 'send-message',
    closeServer: 'close-server'
  };

  static isError(statusCode) {
    return statusCode >= this.SUCCESS_THRESHOLD;
  }
}

module.exports = Pillow;