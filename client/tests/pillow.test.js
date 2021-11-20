const Pillow = require('../pillow');
const PillowError = require('../pillow/error');


describe('Pillow', () => {
  it('Should run request shape validation and throw proper errors', () => {
    const messedUpFields = { data: { username: 'Jack Sparrow', isCaptain: true }, status: 100 };
    // No error messages for 'data' fields since we don't know how to validate it without 'action'
    expect(() => Pillow.validateRequest(messedUpFields)).toThrow(
      new PillowError({
        action: ['This field is required'],
        payload: ['status: This field is not supported']
      })
    );

    const messedUpTypes = { action: new Date(), data: 'The time has come!' };
    expect(() => Pillow.validateRequest(messedUpTypes)).toThrow(
      new PillowError({
        action: ['Expected a String, got Date'],
        data: ['Expected an Object, got String']
      })
    );

    const messedUpChoices = { action: 'find-the-chest', data: { whatWeHave: 'A drawing of a key' } };
    expect(() => Pillow.validateRequest(messedUpChoices)).toThrow(
      new PillowError({
        action: ['Unsupported value find-the-chest, expected one of log-in, send-message, chunks']
      })
    );
  });

  it('Should validate action-specific data and throw proper errors', () => {
    const logInPayload = {
      action: Pillow.actions.logIn,
      data: { message: 'Are you afraid of death?' }
    };
    expect(() => Pillow.validateRequest(logInPayload)).toThrow(
      new PillowError({
        data: [
          'message: This field is not supported',
          'username: This field is required'
        ],
      })
    );

    const sendMessagePayload = {
      action: Pillow.actions.sendMessage,
      data: {}
    };
    expect(() => Pillow.validateRequest(sendMessagePayload)).toThrow(
      new PillowError({
        data: ['Either a message or an attachment must be present']
      })
    );
  });

  it('Should not touch valid payload', () => {
    const validPayload = {
      action: Pillow.actions.sendMessage,
      data: {
        message: 'Call the Kraken!',
        attachment: { file: Buffer.alloc(2), name: 'davy-jones-theme.mp3' }
      }
    };
    expect(Pillow.validateRequest(validPayload)).toEqual(validPayload);
  });
});