const Message = require('./message/index');
const Logger = require('./client/logger');
const dgram = require('dgram');

const f = () => {
  Logger.initTheme();
  // const client = dgram.createSocket('udp4');
  //
  // process.on('SIGINT', () => client.close(() => {
  //   console.log('closed!');
  // }));

  try {
    const mes = Message.makeRequest(0xaa, [
      'google.com',
      'vk.com',
      'mail.google.com',
      'm.vk.com'
    ]);
    console.log(Array.from(mes.values()));

    const p = Message.parse(mes);
    Logger.asList(p);

    // client.send(
    //   mes, 0, mes.byteLength, 53,
    //   '8.8.8.8', (error, bytes) => {
    //     console.log({
    //       error,
    //       bytes
    //     });
    //   }
    // );
    //
    // client.on('message', (msg, rinfo) => {
    //   console.log({
    //     msg,
    //     rinfo
    //   });
    //   Logger.asList(Message.parse(msg));
    // });
    //
    // client.on('error', err => {
    //   console.log(err);
    //   client.close(() => console.log('closed!'));
    // });
  } catch (e) {
    console.log(e);
    // client.close(() => console.log('closed!'));
  }
};


f();
