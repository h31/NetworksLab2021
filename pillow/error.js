class PillowError extends Error {
  errors = null;

  constructor(errors) {
    super();
    this.errors = errors;
  }
}

module.exports = PillowError;