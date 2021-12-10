from datetime import datetime
from message_id import MessageId
import threading
import decoder
import server

lock = threading.Lock()


class ClientThread(threading.Thread):

    def __init__(self, clientsocket, serversocket):
        super().__init__()
        self.clientsocket = clientsocket
        self.serversocket = serversocket
        self.name = ''

    def run(self):
        try:
            while self.serversocket.fileno() != -1:
                msg_arr, msg_time = decoder.decode(self.clientsocket.chat_receive()), \
                                    datetime.timestamp(datetime.now())
                if msg_arr[0] == MessageId.error_message.value:
                    self.clientsocket.chat_send(decoder.encode(msg_arr))
                    continue
                elif msg_arr[0] == MessageId.request_connection.value:
                    lock.acquire()
                    if msg_arr[1] in server.names:
                        self.clientsocket.chat_send(decoder.encode(['0', 'Name ' + msg_arr[1] + ' is already in use.']))
                        lock.release()
                        continue
                    server.names[msg_arr[1]] = self.clientsocket
                    lock.release()
                    self.name = msg_arr[1]
                    self.broadcast(decoder.encode(['2', 'User ' + self.name + ' join the chat.']))
                elif msg_arr[0] == MessageId.send_simple_message.value:
                    msg_arr = ['4', str(msg_time), self.name, msg_arr[1]]
                    self.broadcast(decoder.encode(msg_arr))
                elif msg_arr[0] == MessageId.send_file_message.value:
                    msg_arr = ['6', str(msg_time), self.name, msg_arr[1], msg_arr[2], msg_arr[3]]
                    self.broadcast(decoder.encode(msg_arr))
                elif msg_arr[0] == MessageId.request_close_client.value:
                    self.close()
                    self.clientsocket.chat_send(decoder.encode(['8', 'Disconnect from server.']))
                    return 0
                elif msg_arr[0] == MessageId.request_close_server.value:
                    self.broadcast(decoder.encode(['8', 'User ' + self.name + ' close the chat.']))
                    self.serversocket.close()
                    return 0
                else:
                    self.clientsocket.chat_send(decoder.encode(['0', 'Message is incorrect.']))
        except ConnectionError:
            lock.release()
            if self.name != '':
                self.close()
            return -1

    def broadcast(self, msg):
        cts = list(server.names.values())
        for ct in cts:
            ct.chat_send(msg)

    def close(self):
        lock.acquire()
        if self.name in server.names:
            server.names.pop(self.name)
        lock.release()
        self.broadcast(decoder.encode(['2', 'User ' + self.name + ' left the chat.']))
