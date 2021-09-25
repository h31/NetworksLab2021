// For errors that can't be detected at Pillow level, without some TossServer info
class TossError extends Error {
  errors = {};
  comment = '';

  constructor(errors, comment) {
    super();
    this.errors = errors;
    this.comment = comment || Object.values(errors).flat().join('; ');
  }
}

module.exports = TossError;