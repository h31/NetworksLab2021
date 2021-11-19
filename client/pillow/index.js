const PillowError = require('./error');
const { isEmpty } = require('lodash');
const { capitalCamelCase } = require('../util/misc');
const { checkShape } = require('./validators');

class Pillow {
  static SUCCESS_THRESHOLD = 200;
  static responseStatus = {
    OK: { code: 100, comment: 'Client data received, answer sent' },
    OK_EMPTY: { code: 101, comment: 'Client data received, no data to send back' },
    ERR_REQ_DATA: { code: 200, comment: 'Invalid request data' },
    ERR_REQ_FORMAT: { code: 201, comment: 'Invalid request format' },
    ERR_SERVER: { code: 202, comment: 'Server error' }
  };
  static actions = {
    logIn: 'log-in',
    logOut: 'log-out',
    sendMessage: 'send-message',
    chunks: 'chunks',
    closeServer: 'close-server'
  };

  static requestShape = {
    name: 'payload',
    type: Object,
    required: true,
    topLevel: true,
    fields: [
      {
        name: 'action',
        type: String,
        required: true,
        choices: [this.actions.logIn, this.actions.sendMessage, this.actions.chunks] // logOut and closeServer can only be sent by the server
      },
      {
        name: 'data',
        type: Object,
        required: false
      }
    ]
  };
  static sendMessageShape = {
    name: 'data',
    type: Object,
    required: true,
    fields: [
      {
        name: 'message',
        type: String,
        required: false
      },
      {
        name: 'attachment',
        type: Object,
        required: false,
        fields: [
          {
            name: 'file',
            type: Buffer,
            required: true
          },
          {
            name: 'name',
            type: String,
            required: true
          }
        ]
      }
    ]
  };
  static logInShape = {
    name: 'data',
    type: Object,
    required: true,
    fields: [
      {
        name: 'username',
        type: String,
        required: true
      }
    ]
  };
  static chunksShape = {
    name: 'data',
    type: Object,
    required: true,
    fields: [
      {
        name: 'chunks',
        type: Number,
        required: true
      }
    ]
  };

  static isError(statusCode) {
    return statusCode >= this.SUCCESS_THRESHOLD;
  }

  static validateSendMessage(data) {
    const errors = checkShape(data, this.sendMessageShape);
    if (data && !data.message && !data.attachment) {
      errors.data = ['Either a message or an attachment must be present'];
    }
    if (!isEmpty(errors)) {
      throw new PillowError(errors);
    }
  }

  static validateLogIn(data) {
    const errors = checkShape(data, this.logInShape);
    if (!isEmpty(errors)) {
      throw new PillowError(errors);
    }
  }

  static validateChunks(data) {
    const errors = checkShape(data, this.chunksShape);
    if (!isEmpty(errors)) {
      throw new PillowError(errors);
    }

    if (`${data.chunks}`.includes('.') || data.chunks < 1) {
      throw new PillowError({ data: ['chunks: must be a positive Integer'] });
    }
  }

  static validateRequest(payload) {
    const payloadErrors = checkShape(payload, this.requestShape);
    if (!isEmpty(payloadErrors)) {
      throw new PillowError(payloadErrors);
    }

    const { action, data } = payload;
    this[`validate${capitalCamelCase(action)}`](data);

    return payload;
  }
}

module.exports = Pillow;