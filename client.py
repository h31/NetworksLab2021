import os
import re
import socket
import threading
import datetime as dt


HEADER = 10
IP = '185.183.98.98'
PORT = 6121
ENCODING = 'utf-8'
FILE_NAME_TEMPLATE = r'^[a-zA-Z0-9_.+-]+.\w*%$'


def main():
    my_username = add_nickname()
    client_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client_sock.connect((IP, PORT))
    username = my_username.encode(ENCODING)
    username_header = f"{len(username):<{HEADER}}".encode(ENCODING)
    client_sock.send(username_header + username)
    threading.Thread(target=send, args=(client_sock,)).start()
    threading.Thread(target=receive, args=(client_sock,)).start()


def add_nickname():
    while True:
        nickname = input("Username (No longer than 64 characters): ")
        if not nickname or len(nickname) > 64:
            print("Please enter correct username!")
            continue
        return nickname


def receive_message(sock):
    while True:
        try:
            message_header = sock.recv(HEADER)
            if not len(message_header):
                return False
            message_length = int(message_header.decode(ENCODING))
            return {'header': message_header,
                    'data': sock.recv(message_length)}
        except Exception:
            return


def receive(sock):
    tz = dt.datetime.utcnow().astimezone().tzinfo
    while True:
        try:
            username = receive_message(sock)["data"].decode(ENCODING)
            message = receive_message(sock)["data"].decode(ENCODING)
            if re.match(FILE_NAME_TEMPLATE, message):
                open(message.rstrip("%"), 'wb').write(
                    receive_message(sock)["data"])
                message = f'send {message.rstrip("%")}'
            message_time = receive_message(sock)["data"].decode(ENCODING)
            client_time = dt.datetime.strptime(
                message_time,
                "%d-%m-%Y %H:%M:%S"
            ).now(tz).strftime("%d-%m-%Y %H:%M:%S")
            print(f'<{client_time}>[{username}]: {message}')
        except Exception:
            exit(0)
            return


def send(sock):
    while True:
        try:
            message = input()
            try:
                if message == r'!exit':
                    sock.shutdown(socket.SHUT_RDWR)
                    sock.close()
                    exit(0)
            except Exception:
                exit(0)

            if message and re.match('!upload', message):
                path = os.path.join(message.split(' ')[1])
                file_name_utf = (path.split('/')[-1] + '%').encode(ENCODING)
                file_name_header = f"{len(file_name_utf):<{HEADER}}".encode(
                    ENCODING)
                sock.send(file_name_header + file_name_utf)
                file = open(path, 'rb').read()
                file_header = f"{len(file):<{HEADER}}".encode(ENCODING)
                sock.send(file_header + file)
            elif message:
                message = message.encode(ENCODING)
                message_header = f"{len(message):<{HEADER}}".encode(ENCODING)
                sock.send(message_header + message)
        except KeyboardInterrupt:
            sock.shutdown(socket.SHUT_RDWR)
            sock.close()
            exit(0)
            return
        except Exception:
            print('Closed connection')
            exit(0)
            return


if __name__ == '__main__':
    main()
