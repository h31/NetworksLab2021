class TypedError extends Error {
  static TYPE = {
    redis: 'RedisConnectionError',
    validation: 'ValidationError'
  };

  constructor(message, type) {
    super(message);
    this.type = type;
  }
}

module.exports = TypedError;