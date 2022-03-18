from datetime import datetime
from message_id import MessageId
import decoder


class RequestHandler:

    def __init__(self, clientsocket, serversocket, names):
        self.clientsocket = clientsocket
        self.serversocket = serversocket
        self.names = names
        self.name = ''

    async def run(self):
        while self.serversocket.fileno() != -1:
            msg_arr, msg_time = decoder.decode(await self.clientsocket.chat_receive()), \
                                datetime.timestamp(datetime.now())
            if msg_arr[0] == MessageId.error_message.value:
                await self.clientsocket.chat_send(decoder.encode(msg_arr))
                continue
            elif msg_arr[0] == MessageId.request_connection.value:
                if msg_arr[1] in self.names:
                    await self.clientsocket.chat_send(
                        decoder.encode(['0', 'Name ' + msg_arr[1] + ' is already in use.']))
                    continue
                self.names[msg_arr[1]] = self.clientsocket
                self.name = msg_arr[1]
                await self.broadcast(decoder.encode(['2', 'User ' + self.name + ' join the chat.']))
            elif msg_arr[0] == MessageId.send_simple_message.value:
                msg_arr = ['4', str(msg_time), self.name, msg_arr[1]]
                await self.broadcast(decoder.encode(msg_arr))
            elif msg_arr[0] == MessageId.send_file_message.value:
                msg_arr = ['6', str(msg_time), self.name, msg_arr[1], msg_arr[2], msg_arr[3]]
                await self.broadcast(decoder.encode(msg_arr))
            elif msg_arr[0] == MessageId.request_close_client.value:
                self.close()
                await self.clientsocket.chat_send(decoder.encode(['8', 'Disconnect from server.']))
                return 0
            elif msg_arr[0] == MessageId.request_close_server.value:
                await self.broadcast(decoder.encode(['8', 'User ' + self.name + ' close the chat.']))
                self.serversocket.close()
                return 0
            else:
                await self.clientsocket.chat_send(decoder.encode(['0', 'Message is incorrect.']))

    async def broadcast(self, msg):
        cts = list(self.names.values())
        for ct in cts:
            await ct.chat_send(msg)

    def close(self):
        if self.name in self.names:
            self.names.pop(self.name)
        self.broadcast(decoder.encode(['2', 'User ' + self.name + ' left the chat.']))
