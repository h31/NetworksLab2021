class PillowError extends Error {
  errors = null;

  constructor(errors) {
    super();
    this.errors = errors;
    this.message = JSON.stringify(errors); // for testing purposes
  }
}

module.exports = PillowError;