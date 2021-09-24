const PillowError = require('./error');
const { difference, reverse, isEmpty, isEqual } = require('lodash');
const { capitalCamelCase } = require('../util/misc');

class Pillow {
  static statusNames = {
    OK: 'OK',
    OK_EMPTY: 'OK_EMPTY',
    ERR_REQ_DATA: 'ERR_REQ_DATA',
    ERR_REQ_FORMAT: 'ERR_REQ_FORMAT',
    ERR_SERVER: 'ERR_SERVER',
  }
  static SUCCESS_THRESHOLD = 200;
  static responseStatus = {
    [this.statusNames.OK]: { code: 100, comment: 'Client data received, answer sent' },
    [this.statusNames.OK_EMPTY]: { code: 101, comment: 'Client data received, no data to send back' },
    [this.statusNames.ERR_REQ_DATA]: { code: 200, comment: 'Invalid request data' },
    [this.statusNames.ERR_REQ_FORMAT]: { code: 201, comment: 'Invalid request format' },
    [this.statusNames.ERR_SERVER]: { code: 202, comment: 'Server error' }
  };
  static actions = {
    logIn: 'log-in',
    logOut: 'log-out',
    sendMessage: 'send-message',
  };

  static dataValidationConfig = {
    name: 'payload',
    type: Object,
    required: true,
    topLevel: true,
    fields: [
      {
        name: 'action',
        type: String,
        required: true,
        choices: [this.actions.logIn, this.actions.sendMessage] // logOut can only be sent by the server
      },
      {
        name: 'data',
        type: Object,
        required: false,
        fields: [
          {
            name: 'username',
            type: String,
            required: false
          },
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
                required: true,
              },
              {
                name: 'name',
                type: String,
                required: true
              }
            ]
          }
        ]
      }
    ]
  };

  static isError(statusCode) {
    return statusCode >= this.SUCCESS_THRESHOLD;
  }

  static checkType(val, type) {
    return val && val.constructor.name !== type.name
      ? `Expected a ${type.name}, got ${val.constructor.name}`
      : null;
  }

  static runPrimaryValidation(fieldData, fieldConfig, addErr) {
    const { name, type, required, fields, choices, topLevel } = fieldConfig;

    const typeErr = this.checkType(fieldData, type);
    if (typeErr) {
      addErr(name, typeErr);
      return;
    }

    if (fieldData == null) {
      if (required) {
        addErr(name, 'This field is required');
      }
      return;
    }

    if (choices && !choices.find(variant => isEqual(variant, fieldData))) {
      addErr(name, `Unsupported value: expected one of ${choices.join(', ')}, got ${fieldData}`);
      return;
    }

    if (fields) {
      fields.forEach(subFieldConfig => {
        this.runPrimaryValidation(
          fieldData[subFieldConfig.name],
          subFieldConfig,
          (key, err) => addErr(`${topLevel ? '' : `${name}.`}${key}`, err))
      });

      const possibleFields = fields.map(f => f.name);
      const realFields = Array.from(Object.keys(fieldData));
      const unsupportedFields = difference(realFields, possibleFields);
      if (unsupportedFields.length) {
        addErr(name, `Unsupported fields: ${unsupportedFields.join(', ')}`);
      }
    }
  }

  static checkFields(obj, properFields, required, addErr) {
    const fields = Array.from(Object.keys(obj));
    const checkArgs = [properFields.sort(), fields.sort()];
    const improper = difference(...(required ? checkArgs : reverse(checkArgs)));

    if (improper.length) {
      if (required) {
        improper.forEach(f => addErr(f, 'This field is required'));
      } else {
        addErr('', `These fields: ${improper.join(', ')} are not supported`);
      }
    }
  }

  static validateSendMessage(data, addErr) {
    this.checkFields(data, ['message', 'attachment'], false, addErr);
    if (!data.message && !data.attachment) {
      addErr('', 'Either a message or an attachment must be present');
    }
  }

  static validateLogIn(data, addErr) {
    this.checkFields(data, ['username'], true, addErr);
    this.checkFields(data, ['username'], false, addErr);
  }

  static validateRequest(payload) {
    const errors = {};
    const addErr = (key, val) => {
      if (errors[key]) {
        errors[key].push(val);
      } else {
        errors[key] = [val];
      }
    };
    const checkFinish = () => {
      if (!isEmpty(errors)) {
        throw new PillowError(errors);
      }
    }

    this.runPrimaryValidation(payload, this.dataValidationConfig, addErr);
    checkFinish();

    const { action, data } = payload;

    this[`validate${capitalCamelCase(action)}`](
      data,
      (key, val) => addErr(`data${key ? `.${key}` : ''}`, `${val} when action is ${action}`));
    checkFinish();

    return payload;
  }
}

module.exports = Pillow;