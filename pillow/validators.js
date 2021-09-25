const { difference, reverse, isEqual } = require('lodash');

function checkChoices(data, choices) {
  return !choices || choices.find(choice => isEqual(choice, data))
    ? null
    : `Unsupported value ${data}, expected one of ${choices.join(', ')}`;
}

function checkType(data, type) {
  const article = ['a', 'e', 'i', 'o', 'u'].includes(type.name[0].toLowerCase()) ? 'an' : 'a';
  return data && data.constructor.name !== type.name
    ? `Expected ${article} ${type.name}, got ${data.constructor.name}`
    : null;
}

function checkFields(data, properFields, required) {
  const actualFields = Array.from(Object.keys(data));
  const checkArgs = [properFields.sort(), actualFields.sort()];
  const improperFields = difference(...(required ? checkArgs : reverse(checkArgs)));
  const text = required ? 'This field is required' : 'This field is not supported';
  return improperFields.map(field => `${field}: ${text}`);
}

function checkRequired(data, required) {
  return data == null && required ? 'This field is required' : null;
}

function rearrangeErrors(rawErrors) {
  const result = { ...rawErrors };
  Object.entries(rawErrors).forEach(([key, value]) => {
    if (key.includes('.')) {
      const splitKey = key.split('.');
      const topLevelKey = splitKey[0];
      if (!result[topLevelKey]) {
        result[topLevelKey] = [];
      }
      result[topLevelKey].push(...(value.map(errMsg => `${splitKey.slice(1).join('.')}: ${errMsg}`)));
      delete result[key];
    }
  });
  return result;
}

function checkShape(data, shape, rearrange = true) {
  const errors = {};
  const addErr = (key, val) => {
    if (errors[key]) {
      errors[key].push(val);
    } else {
      errors[key] = [val];
    }
  };

  function checkShapeRecursive(_data, _shape, _addErr) {
    const { name, type, required, fields, choices, topLevel } = _shape;

    const requiredError = checkRequired(_data, required);
    if (requiredError) {
      _addErr(name, requiredError);
    }
    if (_data == null) {
      return;
    }

    const typeError = checkType(_data, type);
    if (typeError) {
      _addErr(name, typeError);
      return;
    }

    const choicesError = checkChoices(_data, choices);
    if (choicesError) {
      _addErr(name, choicesError);
      return;
    }

    if (fields) {
      fields.forEach(fieldShape => checkShapeRecursive(
        _data[fieldShape.name],
        fieldShape,
        (key, val) => _addErr(`${topLevel ? '' : `${name}.`}${key}`, val)
      ));
      const properFields = fields.map(field => field.name);
      const fieldsError = checkFields(_data, properFields, false);
      fieldsError.forEach(oneFieldError => _addErr(name, oneFieldError));
    }
  }

  checkShapeRecursive(data, shape, addErr);
  return rearrange ? rearrangeErrors(errors) : errors;
}

module.exports = {
  checkShape
}


