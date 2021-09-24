const Pillow = require('../pillow/index');
const PillowError = require('../pillow/error');


describe('Pillow', () => {
  it('Should run primary validation and throw proper errors', () => {
    const messedUpFields = { data: { username: 'Jack Sparrow', isCaptain: true }, status: 100 };
    expect(() => Pillow.validateRequest(messedUpFields)).toThrow(
      new PillowError({
        action: ['This field is required'],
        payload: ['Unsupported fields: status'],
        data: ['Unsupported fields: isCaptain']
      })
    );

    const messedUpTypes = { action: new Date(), data: 'The time has come!' };
    expect(() => Pillow.validateRequest(messedUpTypes)).toThrow(
      new PillowError({
        action: 'Expected a String, got Date',
        data: 'Expected an Object, got String'
      })
    );

    const messedUpChoices = { action: 'find-the-chest', data: { whatWeHave: 'A drawing of a key' } };
    expect(() => Pillow.validateRequest(messedUpChoices)).toThrow(
      new PillowError({
        action: ['Unsupported value: expected one of send-message, log-in, got find-the-chest'],
        data: ['Unsupported fields: whatWeHave']
      })
    );
  });

  it('Should validate action-specific data and throw proper errors', () => {
    const logInPayload = {
      action: Pillow.actions.logIn,
      data: { username: 'Davy Jones', message: 'Are you afraid of death?' }
    };
    expect(() => Pillow.validateRequest(logInPayload)).toThrow(
      new PillowError({
        data: ['These fields: message are not supported when action is log-in']
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
        attachment: { file: Buffer.alloc(2), name: 'day-jones-theme.mp3' }
      }
    };
    expect(Pillow.validateRequest(validPayload)).toEqual(validPayload);
  });
});