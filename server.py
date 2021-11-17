import re
import socket
import threading
import datetime as dt


HEADER = 10
IP = '185.183.98.98'
PORT = 6121
ENCODING = 'utf-8'

FILE_NAME_TEMPLATE = r'^[a-zA-Z0-9_.+-]+.\w*%$'

clients = {}


def main():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server.bind((IP, PORT))
    server.listen()
    print(f'Listening for connections on {IP}:{PORT}...')
    while True:
        client_socket, client_address = server.accept()
        user = receive_message(client_socket)
        if user is False:
            continue
        clients[client_socket] = user
        print(f"New connection from {client_address[0]}:{client_address[1]},"
              f" Username: {user['data'].decode(ENCODING)}")
        threading.Thread(target=server_work, args=(client_socket,)).start()


def receive_message(sock):
    try:
        message_header = sock.recv(HEADER)
        if not len(message_header):
            return False
        message_length = int(message_header.decode(ENCODING))
        return {'header': message_header, 'data': sock.recv(message_length)}
    except Exception as e:
        return False


def server_work(sock):
    while True:
        message = receive_message(sock)
        if not message:
            try:
                print('Closed connection from: '
                      f'{clients[sock]["data"].decode(ENCODING)}')
                del clients[sock]
                sock.shutdown(socket.SHUT_RDWR)
                sock.close()
                continue
            except Exception as e:
                continue

        message_time = dt.datetime.utcnow().strftime(
            "%d-%m-%Y %H:%M:%S").encode(ENCODING)
        time_header = f"{len(message_time):<{HEADER}}".encode(ENCODING)
        time = {"header": time_header, "data": message_time}
        user = clients[sock]
        server_time = dt.datetime.now().strftime("%d-%m-%Y %H:%M:%S")
        if re.match(FILE_NAME_TEMPLATE, message["data"].decode(ENCODING)):
            file = receive_message(sock)
            print(f'{server_time} File with name '
                  f'{message["data"].decode(ENCODING).rstrip("%")} from '
                  f'{user["data"].decode(ENCODING)}')
            for client_socket in clients:
                if client_socket != sock:
                    client_socket.send(
                        user['header'] + user['data'] +
                        message['header'] + message['data'] +
                        file['header'] + file['data'] +
                        time['header'] + time['data']
                    )
        else:
            print(f'{server_time} Message from {user["data"].decode(ENCODING)}'
                  f': {message["data"].decode(ENCODING)}')
            for client_socket in clients:
                if client_socket != sock:
                    client_socket.send(
                        user['header'] + user['data'] +
                        message['header'] + message['data'] +
                        time['header'] + time['data']
                    )


if __name__ == '__main__':
    main()
