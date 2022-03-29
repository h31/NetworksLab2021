import socket
import threading
import time
from commands import Command


FORMAT = 'utf-8'

SERVER = "192.168.0.107"
PORT = 5005
ADDR = (SERVER, PORT)

client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
client.connect(ADDR)


def get_type(msg):
    msg_type = 0
    if msg.startswith(Command.DISCONNECT_COMMAND.value):
        msg_type = 1
    elif msg.startswith(Command.LOGIN_COMMAND.value):
        msg_type = 2
    return str(msg_type)


def send_message():
    while True:
        msg = input()
        msg_type = get_type(msg)
        eom = "0"
        msg = msg.encode(FORMAT)

        full_len = len(msg)
        th = full_len // 1000
        for i in range(0, th):
            part = str(i) + " " * (6 - len(eom))
            part += msg_type + " " * (6 - len(msg_type))
            part += eom + " " * (6 - len(msg_type))
            part += "1000" + " " * (6 - len("1000"))
            part = part.encode(FORMAT)
            part += msg[i*1000: i*1000 + 1000]
            client.sendto(part, ADDR)

        eom = "1"
        x = full_len % 1000

        end = str(th) + " " * (6 - len(str(th)))
        end += msg_type + " " * (6 - len(msg_type))
        end += eom + " " * (6 - len(eom))
        end += str(x) + " " * (6 - len(str(x)))
        end = end.encode(FORMAT)
        end += msg[full_len - full_len % 1000 : full_len]
        client.sendto(end, ADDR)


def decompose_message(msg):
    msg_id = int(msg[0:6])
    msg_type = int(msg[6:12])
    eom = int(msg[12:18])
    msg_length = int(msg[18:24])
    msg_body = msg[24:]
    return msg_id, msg_type, eom, msg_length, msg_body


def get_message():
    while True:
        msg = client.recv(1024)
        msg_id, msg_type, eom, msg_length, msg_body = decompose_message(msg)
        if msg_type == 3:
            auth = msg_body.decode(FORMAT)
            new_msg = client.recv(1024)
            msg_id, msg_type, eom, msg_length, msg_body = decompose_message(new_msg)
            while eom != 1:
                new_msg = client.recv(1024)
                msg_body += decompose_message(new_msg)[4]
            msg_body = msg_body.decode(FORMAT)
            cur_time = time.asctime()
            print(f"<{cur_time}> [{auth}] {msg_body}")
        elif msg_type == 1 or msg_type == 2:
            print("[SERVER] " + msg_body.decode(FORMAT))


sending_thr = threading.Thread(target=send_message)
getting_thr = threading.Thread(target=get_message)
sending_thr.start()
getting_thr.start()
print("You will not be able to send and get messages until login")
print(f"To login use {Command.LOGIN_COMMAND.value} <nickname>")