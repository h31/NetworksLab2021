from extended_socket import ChatSocket
from message_id import MessageId
from datetime import datetime
import threading
import decoder


def request_connection(socket):
    while True:
        name = input()
        socket.chat_send(decoder.encode([MessageId.request_connection.value, name]))
        answer = s.chat_receive()
        name_answer = decoder.decode(answer)
        if name_answer[0] == MessageId.error_message.value:
            print(name_answer[1] + ' Please try again:')
            continue
        elif name_answer[0] == MessageId.accept_connection.value:
            print(name_answer[1])
            break


def client_receive(socket):
    while socket.sock.fileno() != -1:
        msg_arr = decoder.decode(socket.chat_receive())
        if msg_arr[0] == MessageId.error_message.value:
            print(msg_arr[1])
        if msg_arr[0] == MessageId.accept_connection.value:
            print(msg_arr[1])
        if msg_arr[0] == MessageId.receive_simple_message.value:
            print(
                datetime.fromtimestamp(float(msg_arr[1])).strftime("<%H:%M>") + '[' + msg_arr[2] + ']' + msg_arr[3]
            )
        if msg_arr[0] == MessageId.receive_file_message.value:
            print(
                datetime.fromtimestamp(float(msg_arr[1])).strftime("<%H:%M>") +
                '[' + msg_arr[2] + ']' + msg_arr[3] + '(' + msg_arr[4] + ')')
            f_out = open(msg_arr[4], 'wb')
            f_out.write(msg_arr[5])
            f_out.close()
        if msg_arr[0] == MessageId.receive_close_client.value:
            print(msg_arr[1])
            socket.sock.close()


def client_send(socket):
    while True:
        new_msg = input()
        if new_msg == '#quit':
            socket.chat_send(decoder.encode([MessageId.request_close_client.value]))
            break
        if new_msg == '#close':
            socket.chat_send(decoder.encode([MessageId.request_close_server.value]))
            break
        msg_parse = decoder.decode(decoder.encode(['3', new_msg]))
        if len(msg_parse) == 3:
            try:
                f = open(msg_parse[-1], 'rb')
                data = f.read()
                f.close()
                socket.chat_send(decoder.encode([MessageId.send_file_message.value, new_msg, data]))
            except FileNotFoundError:
                print('File ' + msg_parse[-1] + ' not founded.')
        else:
            socket.chat_send(decoder.encode([MessageId.send_simple_message.value, new_msg]))


if __name__ == '__main__':
    s = ChatSocket()
    try:
        s.sock.connect(('localhost', 1024))
    except ConnectionRefusedError:
        print('Unable to connect to the server.')
        exit(-1)
    print('Connected to server. Please write your name:')
    try:
        request_connection(s)
        send_thread = threading.Thread(target=client_send, args=(s,))
        send_thread.daemon = True
        send_thread.start()
        client_receive(s)
    except ConnectionError:
        print("Connection broken")
        s.sock.close()
        exit(-1)
