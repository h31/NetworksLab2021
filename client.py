import socket
import threading
import sys
import datetime
import time
from datetime import datetime

FORMAT = 'UTF-8'
HEADER = 64

address = input("Waiting for server IP address: ")
port = int(input("Waiting for server port "))
setup = (address, port)
client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(setup)
name = input('Input your name:').encode(FORMAT)
username = f"{len(name):<{HEADER}}".encode(FORMAT) + name
client.send(username)
timezone_offset = - time.timezone / 3600


def send_server():
    sock = threading.Thread(target=stay_connected())
    sock.start()
    while True:
        try:
            message = input()
            if message:
                message = message_preparation(message)
                message = f'{len(message):<{HEADER}}'.encode(FORMAT) + message
                client.send(message)
        except:
            sys.exit(0)


def stay_connected():
    while True:
        try:
            message_header = client.recv(HEADER)
            if not len(message_header):
                print('Enter any key to exit')
                sys.exit(0)
            message_lenthg = int(message_header.decode(FORMAT))
            message = client.recv(message_lenthg)
            message = [m.decode(FORMAT) for m in message.split(b'\0')]
            if len(message) == 1:
                print(message[0])
            else:
                message[0] = message[0]
                message_reader(message)
        except:
            sys.exit(0)


def message_preparation(message):
    hour = datetime.now().hour
    minute = datetime.now().minute
    timeline = (str(hour) + ':' + str(minute) + ' ' + str(timezone_offset)).encode(FORMAT)
    return b'\0'.join([timeline, name, message.encode(FORMAT)])


def time_set(hour, minute):
    return hour + ':' + minute


def message_reader(message):
    work_time = message[0].split(' ')
    if work_time[1] != str(timezone_offset):
        first = timezone_offset - float(work_time[1])
        hour, minute = work_time[0].split(':')
        offset = int(first)
        hour = int(hour) + offset
        if hour > 23:
            hour = hour - 24
            current_time = time_set(str(hour), minute)
            current_name = message[1]
            current_msg = message[2]
            print(current_time + ' [ ' + current_name + ' ]:' + current_msg)
        elif hour < 0:
            hour = 24 + hour
            current_time = time_set(str(hour), minute)
            current_name = message[1]
            current_msg = message[2]
            print(current_time + ' [ ' + current_name + ' ]:' + current_msg)
        else:
            current_time = time_set(str(hour), minute)
            current_name = message[1]
            current_msg = message[2]
            print(current_time + ' [ ' + current_name + ' ]: ' + current_msg)
    else:
        current_name = message[1]
        current_msg = message[2]
        print(work_time[0] + ' [ ' + current_name + ' ]:' + current_msg)


if __name__ == '__main__':
    send_server()
