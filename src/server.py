import re
import socket
from datetime import datetime
from select import select
import time as tm

HEADER = 10
READ = 10000
# IP = '185.183.98.98'
IP = 'localhost'
PORT = 8000
CODE = 'utf-8'
FILE_NAME_TEMPLATE = r'^[a-zA-Z0-9_.+-]+.\w*%$'
HEADER_LENGTH = 10

clients = {}
socket_list = []
buffer = {}


def main():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind((IP, PORT))
    server_socket.setblocking(False)
    server_socket.listen()
    socket_list.append(server_socket)
    print(f'Listening for connections on {IP}:{PORT}...')
    server(server_socket)


def client_connect(sock):
    client_socket, client_address = sock.accept()
    client_socket.setblocking(False)
    buffer[client_socket] = {'message': b'', 'full': False, 'length': 0, 'have_len': False}
    user = receive_full_message(client_socket)
    socket_list.append(client_socket)
    clients[client_socket] = user
    print(f"New connection from {client_address[0]}:{client_address[1]}, Username: {user['data'].decode(CODE)}")


def receive_header(sock):
    try:
        message_header = sock.recv(HEADER_LENGTH)
        if not len(message_header):
            return False
        message_length = int(message_header.decode(CODE))
        buffer[sock]['length'] = message_length
        buffer[sock]['have_len'] = True
    except ConnectionResetError:
        return False


def receive_data(sock, data, message_length):
    if not message_length:
        return False
    data += sock.recv(min(READ, message_length - len(data)))
    if len(data) == message_length:
        return {'data': data, 'full': True}
    else:
        return {'data': data, 'full': False}


def receive_full_message(sock):
    if not buffer[sock]['have_len']:
        receive_header(sock)
    length = buffer[sock]['length']
    if not buffer[sock]['full']:
        data = buffer[sock]['message']
        message = receive_data(sock, data, length)
        if not message:
            print(f'Closed connection from: {clients[sock]["data"].decode(CODE)}')
            del clients[sock]
            del buffer[sock]
            socket_list.remove(sock)
            sock.shutdown(socket.SHUT_RDWR)
            sock.close()
            return
        buffer[sock]['message'] = message['data']
        buffer[sock]['full'] = message['full']
    if buffer[sock]['full']:
        message = buffer[sock]['message']
        buffer[sock] = {'message': b'', 'full': False, 'length': 0, 'have_len': False}
        return {'header': f"{len(message):<{HEADER_LENGTH}}".encode(CODE), 'data': message}


def server(sock):
    try:
        while True:
            read_sockets, write_sockets, exception_sockets = select(socket_list, [], socket_list, 1)
            for read_socket in read_sockets:
                if read_socket == sock:
                    client_connect(sock)
                else:
                    message = receive_full_message(read_socket)
                    print("message = ", message)
                    if message:
                        message_time = datetime.utcnow().strftime("%d-%m-%Y %H:%M:%S").encode(CODE)
                        time_header = f"{len(message_time):<{HEADER_LENGTH}}".encode(CODE)
                        time = {"header": time_header, "data": message_time}
                        user = clients[read_socket]

                        server_time = datetime.now().strftime("%d-%m-%Y %H:%M:%S")
                        if re.match(FILE_NAME_TEMPLATE, message["data"].decode(CODE)):
                            tm.sleep(0.01)
                            file = receive_full_message(read_socket)
                            print("file = ", file)
                            print(f'{server_time} File with name '
                                  f'{message["data"].decode(CODE).rstrip("%")} from '
                                  f'{user["data"].decode(CODE)}')
                            for client_socket in clients:
                                if client_socket != read_socket:
                                    client_socket.send(
                                        user['header'] + user['data'] +
                                        message['header'] + message['data'] +
                                        file['header'] + file['data'] +
                                        time['header'] + time['data']
                                    )
                        else:
                            print(f'{server_time} Message from {user["data"].decode(CODE)}'
                                  f': {message["data"].decode(CODE)}')
                            for client_socket in clients:
                                if client_socket != read_socket:
                                    client_socket.send(
                                        user['header'] + user['data'] +
                                        message['header'] + message['data'] +
                                        time['header'] + time['data']
                                    )

    except KeyboardInterrupt:
        sock.shutdown(socket.SHUT_RDWR)
        sock.close()
        exit(0)


if __name__ == '__main__':
    main()
