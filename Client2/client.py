# coding=utf-8
import os
import re
import signal
import socket
import threading
from datetime import datetime

HEADER_LENGTH = 10
SEPARATOR = "<SEPARATOR>"
SEND_FILE = "SEND_FILE"

IP = "networkslab-ivt.ftp.sh"
PORT = 10000
UID = "c97ec0d1-df22-41f4-858f-7beee9e1bbc4".encode("utf-8")
CONNECT = "CONNECT"
DISCONNECT = "DISCONNECT"


def main():
    nickname = input("Write name for chat: ")
    clientsocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    clientsocket.connect((IP, PORT))

    def catch_interrupt(signal, frame):
        clientsocket.shutdown(socket.SHUT_WR)
        clientsocket.close()
        os._exit(0)

    signal.signal(signal.SIGINT, catch_interrupt)
    nickname_code = nickname.encode('utf-8')
    nickname_header = f"{len(nickname_code):<{HEADER_LENGTH}}".encode('utf-8')
    clientsocket.send(nickname_header + nickname_code)
    receive_thread = threading.Thread(target=getMessage, args=(clientsocket,))
    receive_thread.start()
    try:
        while True:
            message = input()
            if (re.match("^send .*\s*$", message)):
                try:
                    files = re.findall(r'[^send].*', message)
                    fileName = files[0].strip(' ')
                    filesize = os.path.getsize(fileName)
                    clientsocket.send(f"{SEND_FILE:<{HEADER_LENGTH}}".encode("utf-8"))
                    fileHeader = f"{fileName}{SEPARATOR}{filesize}".encode()
                    clientsocket.send(f"{len(fileHeader):<{HEADER_LENGTH}}".encode('utf-8'))
                    clientsocket.send(fileHeader)
                    f = open(fileName, "rb")
                    bytes_read = f.read(filesize)
                    clientsocket.sendall(bytes_read)
                    f.close()
                    clientsocket.send(UID)
                except:
                    print(f'Not can find {fileName}')

            else:
                if message:
                    message_code = message.encode('utf-8')
                    message_header = f"{len(message_code):<{HEADER_LENGTH}}".encode(
                        'utf-8')
                    clientsocket.send(
                        message_header + message_code)
    except:
        os._exit(0)


def getMessage(clientsocket):
    while True:
        nickname_header = clientsocket.recv(HEADER_LENGTH)
        current_time = datetime.now().strftime("%H:%M")
        if len(nickname_header) == 0:
            print("Close connect")
            clientsocket.shutdown(socket.SHUT_WR)
            clientsocket.close()
            os._exit(0)
        if nickname_header.decode().strip() == SEND_FILE:
            name_header = clientsocket.recv(HEADER_LENGTH)
            name_length = int(name_header.decode())
            name = clientsocket.recv(name_length).decode()
            file_header_len = int(clientsocket.recv(HEADER_LENGTH).decode())
            file_header = clientsocket.recv(file_header_len)
            filename, filesize = file_header.decode('utf-8').split(SEPARATOR)
            filename = os.path.basename(filename)
            filesize = int(filesize)
            f = open(filename, "wb")
            total_bytes = bytes()
            while not UID in total_bytes:
                bytes_read = clientsocket.recv(filesize)
                total_bytes += bytes_read
            f.write(total_bytes[:(len(total_bytes) - len(UID))])
            f.close()
            print(f'<{current_time}> {name} send file {filename}')
        else:
            type = nickname_header.decode('utf-8').strip()
            if type == CONNECT or type == DISCONNECT:
                warning_length = int(
                    clientsocket.recv(HEADER_LENGTH).decode('utf-8').strip())
                warning = clientsocket.recv(warning_length).decode('utf-8')
                print(f'{warning}')
                continue
            nickname_length = int(nickname_header.decode('utf-8').strip())
            nickname = clientsocket.recv(nickname_length).decode('utf-8')
            message_header = clientsocket.recv(HEADER_LENGTH)
            message_length = int(message_header.decode('utf-8').strip())
            message = clientsocket.recv(message_length).decode('utf-8')
            print(f'<{current_time}> [{nickname}]: {message}')


if __name__ == '__main__':
    main()
