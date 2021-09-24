class PillowError extends Error {
  errors = null;

  constructor(errors) {
    super();
    this.errors = this._rearrangeErrors(errors);
  }

  _rearrangeErrors(rawErrors) {
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
}

module.exports = PillowError;