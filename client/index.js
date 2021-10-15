const dgram = require('dgram');

const client = dgram.createSocket('udp4');

console.log('Wait a bit....')
setTimeout(() => {
  const msg = Buffer.from('Aboba');
  client.send(msg, 0, msg.byteLength, 41234, 'localhost', (error, bytes) => {
    console.log({
      error,
      bytes
    });
  })
}, 1500);