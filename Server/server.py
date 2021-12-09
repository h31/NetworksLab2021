# coding=utf-8
import socket
from datetime import datetime
import os
import select
import time

HEADER_LENGTH = 10
IP = "127.0.0.1"
PORT = 10000
clients = {}
SEND_FILE = "SEND_FILE"
SEPARATOR = "<SEPARATOR>"
CONNECT = "CONNECT"
DISCONNECT = "DISCONNECT"
UID = "c97ec0d1-df22-41f4-858f-7beee9e1bbc4".encode("utf-8")


def main():
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((IP, PORT))
    server.listen(5)
    server.setblocking(False)
    print("Server started!!!")
    inputs = [server]
    try:
        while True:
            reads, send, excepts = select.select(inputs, [], inputs)
            for conn in reads:
                if conn == server:
                    new_conn, client_addr = conn.accept()
                    new_conn.setblocking(False)
                    inputs.append(new_conn)
                else:
                    current_time = datetime.now().strftime("%H:%M")
                    if conn not in clients.keys():
                        while True:
                            header = conn.recv(HEADER_LENGTH)
                            header_len = int(header.decode('utf-8').strip())
                            name = conn.recv(header_len).decode("utf-8")
                            if name in clients.values():
                                code_n = f'{DISCONNECT:<{HEADER_LENGTH}}'.encode('utf-8')
                                notice = f"Sorry, already have a client with this name: {name} " \
                                         f"Need choose other name".encode('utf-8')
                                notice_header = f"{len(notice):<{HEADER_LENGTH}}".encode('utf-8')
                                message = code_n + notice_header + notice
                                conn.send(message)
                                conn.close()
                            else:
                                clients[conn] = name
                                print(f"In {current_time} connected new client - {name}")
                                notificationForClient(CONNECT, conn)
                            break
                    else:
                        while True:
                            try:
                                header = conn.recv(HEADER_LENGTH)
                                name = clients[conn].encode("utf-8")
                                if header.decode('utf-8').strip() != SEND_FILE:
                                    header_name = f'{len(name):<{HEADER_LENGTH}}'.encode('utf-8')
                                    message = conn.recv(int(header))
                                    print(
                                        f'in {current_time}  recieved message from {clients[conn]}: {message.decode("utf-8")}')
                                    final_message = header_name + name + header + message
                                    sendToAll(final_message, conn)
                                else:
                                    file_header_len_header = conn.recv(HEADER_LENGTH).decode("utf-8")
                                    file_header_len = int(file_header_len_header)
                                    file_header = conn.recv(file_header_len)
                                    filename, filesize = file_header.decode('utf-8').split(SEPARATOR)
                                    filesize = int(filesize)
                                    total_bytes = bytes()
                                    time.sleep(1)
                                    try:
                                        while not UID in total_bytes:
                                            bytes_read = conn.recv(filesize)
                                            total_bytes += bytes_read
                                    finally:
                                        file_header_len = f'{len(file_header):<{HEADER_LENGTH}}'.encode('utf-8')
                                        name_header = f'{len(name):<{HEADER_LENGTH}}'.encode('utf-8')
                                        sendFileFlag = f'{SEND_FILE:<{HEADER_LENGTH}}'.encode('utf-8')
                                        fileMessage = sendFileFlag + name_header + name + file_header_len + file_header + total_bytes
                                        print(
                                            f"In {current_time} client {clients[conn]} send file {filename}")
                                        sendToAll(fileMessage, conn)
                            except:
                                inputs.remove(conn)
                                print(f"{clients[conn]} disconnected")
                                notificationForClient(DISCONNECT, conn)
                                del clients[conn]
                                conn.close()
                                break

                            break

            for conn in excepts:
                print(f"{clients[conn]} disconnected")
                notificationForClient(DISCONNECT, conn)
                inputs.remove(conn)
                conn.close()
                del clients[conn]

    except KeyboardInterrupt:
        for cl in clients:
            cl.shutdown(socket.SHUT_WR)
            cl.close()
        server.shutdown(socket.SHUT_WR)
        server.close()
        os._exit(0)


def sendToAll(msg, clientsocket):
    for client in clients:
        if client != clientsocket:
            client.send(msg)


def notificationForClient(type, clientsocket):
    code_n = f'{type:<{HEADER_LENGTH}}'.encode('utf-8')
    notice: bytes
    name = clients[clientsocket]
    if type == CONNECT:
        notice = f"{name} join chat ".encode('utf-8')
    if type == DISCONNECT:
        notice = f"{name} closed chat ".encode('utf-8')
    notice_header = f"{len(notice):<{HEADER_LENGTH}}".encode('utf-8')
    message = code_n + notice_header + notice
    sendToAll(message, clientsocket)


if __name__ == '__main__':
    main()
