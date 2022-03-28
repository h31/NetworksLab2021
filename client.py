import socket
import threading
import sys
import datetime
import time
from datetime import datetime

SERVER = socket.gethostbyname(socket.gethostname())

ENCOD = 'utf-8'
PORT = 1339
HEADER = 64
CHECK = True
ADDRESS = (SERVER, PORT)
wellcome_text = input('Input your name:')

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(ADDRESS)

name = wellcome_text.encode(ENCOD)
username_msg = f"{len(name):<{HEADER}}".encode(ENCOD) + name
client.send(username_msg)


def listener(message):
    work_time = message[0].split(' ')
    client_timezone = - time.timezone / 3600
    current_time = work_time[0]
    if work_time[1] != str(client_timezone):
        number1 = client_timezone - float(work_time[1])
        hour, minute = work_time[0].split(':')
        df = int(number1)
        hour = int(hour) + df
        if hour > 23:
            hour = hour - 24
        elif hour < 0:
            hour = 24 + hour
        current_time = time_set(str(hour), minute)
    current_name = message[1]
    current_msg = message[2]
    print(current_time + ' [ ' + current_name + ' ]:' + current_msg)


def time_set(h, m):
    return h + ':' + m


def encoding(message):
    hour = datetime.now().hour
    minute = datetime.now().minute
    nnn = - time.timezone / 3600

    timeline = (str(hour) + ':' + str(minute) + ' ' + str(nnn)).encode(ENCOD)
    usr = wellcome_text.encode(ENCOD)
    msg = message.encode(ENCOD)
    return b'\0'.join([timeline, usr, msg])


def read():
    global CHECK
    while CHECK:
        try:
            mheader = client.recv(HEADER)
            if not len(mheader):
                print('Enter any key to exit')
                CHECK = False
                sys.exit(0)

            msg_lenthg = int(mheader.decode(ENCOD))
            msg = client.recv(msg_lenthg)
            nnn = msg_lenthg - len(msg)
            while nnn != 0:
                msg += client.recv(msg_lenthg)
                nnn = msg_lenthg - len(msg)

            msg = [m.decode(ENCOD) for m in msg.split(b'\0')]
            if len(msg) == 1:
                print(msg[0])
            else:
                msg[0] = msg[0]
                listener(msg)
        except:
            CHECK = False
            sys.exit(0)


def send():
    threading.Thread(target=read).start()
    while CHECK:
        try:
            mes = input()
            if mes and CHECK:
                msg = encoding(mes)
                msg = f'{len(msg):<{HEADER}}'.encode(ENCOD) + msg
                client.send(msg)
        except:
            sys.exit(0)


if __name__ == '__main__':
    send()
