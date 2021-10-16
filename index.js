const Message = require('./message/index');
const Logger = require('./client/logger');
const dgram = require('dgram');
const BitBuffer = require('./bit-buffer/index');
const ResourceRecord = require('./resource-record/index');

const f = () => {
  Logger.initTheme();
  const client = dgram.createSocket('udp4');

  process.on('SIGINT', () => client.close(() => {
    console.log('closed!');
  }));

  try {
    const mes = Message.makeRequest(
      0xaa,
      ['ns1.vkontakte.ru'],
      { qType: ResourceRecord.TYPE.ipv6 }
    );
    // console.log(Array.from(mes.values()));

    // const p = Message.parse(mes);
    // Logger.asList(p);
    // Logger.asTable([
    //   [16],
    //   [1, 4, 1, 1, 1, 1, 3, 4],
    //   [16], [16], [16], [16]
    // ], (new BitBuffer(mes)).data)

    client.send(
      mes, 0, mes.byteLength, 53,
      '8.8.8.8', (error, bytes) => {
        console.log({
          error,
          bytes
        });
      }
    );

    client.on('message', (msg, rinfo) => {
      console.log({
        msg,
        rinfo
      });
      Logger.asList(Message.parse(msg));
    });

    client.on('error', err => {
      console.log(err);
      client.close(() => console.log('closed!'));
    });
  } catch (e) {
    console.log(e);
    client.close(() => console.log('closed!'));
  }
};


f();
