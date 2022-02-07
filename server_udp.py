import socket
import threading
from datetime import datetime
from pytz import timezone

code_table = 'utf-8'

hostS = 'networkslab-ivt.ftp.sh'
hostL = '127.0.0.1'
port = 55555
file_end = '37e3f4a8-b8c9-4f22-ad4d-8bd81e686822'
length_of_message = len(f"file{file_end}")

server = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
server.bind((hostL, port))
u_sockets = []
lenght = 10


def broadcast(message, addres):
    for client in u_sockets:
        if client != addres:
            server.sendto(message, client)


# отправлять по адресам, принимать сервером server.recvfrom(lenght)
# отправлять от сервера то есть server.sendto(message, addres). адреса в users


# def handle(message, address):
def handle():
    global u_sockets
    buffer = 0
    while True:
        full_mes = ''.encode(code_table)
        message = ''.encode(code_table)
        message, address = server.recvfrom(lenght)
        if address not in u_sockets:
            print(f"Connected with {str(address)}")
            u_sockets.append(address)
        else:
            full_mes += message
            # это часть модернизируется с целью успешного получения всего сообщения при выкладке сервера удаленно
            while not file_end.encode(code_table) in full_mes:
                message, address = server.recvfrom(lenght)
                full_mes += message
            else:
                time_zone = full_mes[
                            full_mes.find('<'.encode(code_table)) + 1: full_mes.find('>'.encode(code_table))]
                now_time = datetime.now(timezone(time_zone)).strftime(
                    "%Y-%m-%d %H:%M")  # время сервера измененное в соответствии с tz пользователя
                message_send = '<'.encode(code_table) + now_time.encode(code_table) + '> '.encode(code_table) \
                               + full_mes[
                                 full_mes.find('>'.encode(code_table)) + 1:]
                broadcast(message_send, address)
                print("sev2")


thread = threading.Thread(target=handle)
thread.start()