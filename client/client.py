import socket
import struct
import threading
from datetime import datetime
import sys
sys.path.append('..')
from tools import getNonExistentName


def safeRecv(size: int):
    msg = bytes()
    while size != 0:
        try:
            msg_tmp = sock.recv(size)
        except:
            disconnect_server()
            exit(1)
        size -= len(msg_tmp)
        msg += msg_tmp
    return msg


def clientRecv():
    (time_size,) = struct.unpack(">I", safeRecv(4))
    (name_size,) = struct.unpack(">I", safeRecv(4))
    (msg_size,) = struct.unpack(">I", safeRecv(4))
    time = safeRecv(time_size).decode()
    name = safeRecv(name_size).decode()
    msg = safeRecv(msg_size - 1).decode()
    flags = safeRecv(1)[0]
    if flags:
        (path_size,) = struct.unpack(">I", safeRecv(4))
        (file_size,) = struct.unpack(">I", safeRecv(4))
        file_path = safeRecv(path_size).decode()
        file = safeRecv(file_size)

        actual_path = getNonExistentName(file_path)
        f = open(actual_path, "w+b")
        f.write(file)
        f.close()
    else:
        actual_path = None
    return time, name, msg, actual_path


def printMsg(time, name_msg, msg, file_path):
    time = getLocalTime(time)
    if file_path is not None:
        s = f' ({file_path} attached)'
    else:
        s = ""
    print(f'\n<{time}>[{name_msg}] {msg}{s}\n')


def backFunc():
    while True:
        msg_time, msg_name, msg_msg, msg_file_path = clientRecv()
        printMsg(msg_time, msg_name, msg_msg, msg_file_path)




def clientSend(msg: bytes, path=None):
    flags = 1 * (path is not None)
    msg = bytearray(msg)
    msg.append(flags)
    msg = bytes(msg)
    msg_size = len(msg)

    size_packed = struct.pack(">I", msg_size)

    sock.send(size_packed)
    sock.send(msg)
    if path is not None:
        f = open(path, "rb")
        file = f.read()
        f.close()

        file_path = path.encode()
        file_path_size_packed = struct.pack(">I", len(file_path))

        file_size_packed = struct.pack(">I", len(file))

        sock.send(file_path_size_packed)
        sock.send(file_size_packed)
        sock.send(file_path)
        sock.send(file)


def disconnect_server():
    print("SERVER DISCONNECT")
    diseconnect_event.set()


def getLocalTime(utcTime):
    return str(datetime.fromisoformat(utcTime).astimezone().strftime('%H:%M'))


sock = socket.socket()
sock.connect(('127.0.0.1', 9090))

max_name_len = 32  # limit on client side
while True:
    name = input(f'Enter your name({max_name_len} symbols max):').encode()
    if len(name) > max_name_len:
        print("Name so long, try again")
    else:
        clientSend(name)
        break

diseconnect_event = threading.Event()
listenThread = threading.Thread(target=backFunc)
listenThread.start()

while True:
    msg = input(f'Enter massage: ')
    file_path = input(f'path to the attached file:')

    if len(file_path) == 0:
        file_path = None
    if diseconnect_event.is_set():
        break
    clientSend(msg.encode(), file_path)

listenThread.join()
sock.close()
