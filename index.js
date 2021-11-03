// // const Message = require('./common-classes');
// // const Logger = require('./client/logger');
// // const dgram = require('dgram');
// // const BitBuffer = require('./common-classes');
// // const ResourceRecord = require('./common-classes');
// //
// // const f = () => {
// //   Logger.initTheme();
// //   const client = dgram.createSocket('udp4');
// //
// //   process.on('SIGINT', () => client.close(() => {
// //     console.log('closed!');
// //   }));
// //
// //   try {
// //     const mes = Message.makeRequest(
// //       0xaa,
// //       ['95.211.37.196'],
// //       { opCode: Message.OPCODE.inverseQuery }
// //     );
// //     // console.log(Array.from(mes.values()));
// //
// //     // const p = Message.parse(mes);
// //     // Logger.asList(p);
// //     console.log('HEADER'.red);
// //     Logger.asTable([
// //       [16],
// //       [1, 4, 1, 1, 1, 1, 3, 4],
// //       [16], [16], [16], [16]
// //     ], (new BitBuffer(mes)).data);
// //     console.log('BODY'.red);
// //     console.log(Array.from(mes.values()).slice(12));
// //
// //     client.send(
// //       mes, 0, mes.byteLength, 53,
// //       '8.8.8.8', (error, bytes) => {
// //         console.log({
// //           error,
// //           bytes
// //         });
// //       }
// //     );
// //
// //     client.on('message', (msg, rinfo) => {
// //       console.log({
// //         msg,
// //         rinfo
// //       });
// //       Logger.asList(Message.parse(msg));
// //     });
// //
// //     client.on('error', err => {
// //       console.log(err);
// //       client.close(() => console.log('closed!'));
// //     });
// //   } catch (e) {
// //     console.log(e);
// //     client.close(() => console.log('closed!'));
// //   }
// // };
// //
// //
// // //f();
// //
// // const yargs = require('yargs/yargs');
// //
// // const a = yargs().parse(['127.0.0.1', '--display', 't']);
// //
// // console.log(a);
//
const yargs = require('yargs/yargs');


const { hideBin } = require('yargs/helpers');


const COMMANDS = {
  server: 'server',
  client: 'client'
};
const DEMAND_COMMAND_MSG = 'You have to specify one and only one command to run';

yargs(hideBin(process.argv))
  .command({
    command: COMMANDS.server,
    desc: 'Start server',
    handler: require('./server'),
    builder: (_yargs) => _yargs.options({
      port: { type: 'number', default: 41234 }
    })
  })
  .command({
    command: COMMANDS.client,
    desc: 'Start client',
    handler: require('./client')
  })
  .demandCommand(1, 1, DEMAND_COMMAND_MSG, DEMAND_COMMAND_MSG)
  .options({
    database: { type: 'number', desc: 'Redis database to use (create new if nothing provided)' },
  })
  .parse();

// const redis = require('redis');
//
// const woo = async () => {
//   const rc = redis.createClient();
//   await rc.connect();
//   const nl = await rc.sAdd('aboba', '12');
//   console.log(nl);
//
//   await rc.quit();
// }
//
// woo()