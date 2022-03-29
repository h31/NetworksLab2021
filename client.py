import socket
import threading
import sys

FORMAT = 'UTF-8'
name = input("Please enter your name: ")
user = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
user.connect(('localhost', 10000))


def message_receive():
    while True:
        try:
            message = user.recv(16384).decode(FORMAT)
            if message == 'NAME':
                user.send(name.encode(FORMAT))
            else:
                print(message)
        except:
            sys.exit(0)


def message_send():
    while True:
        try:
            mes = input("")
            message = '{}:{}'.format(name, mes)
            user.send(message.encode(FORMAT))
        except:
            sys.exit(0)


message_receive_thread = threading.Thread(target=message_receive)
message_receive_thread.start()
message_send_thread = threading.Thread(target=message_send)
message_send_thread.start()
