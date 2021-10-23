import socket
import struct
import threading
from datetime import datetime
from pathlib import Path


def saveRecv(size: int):
    msg = bytes()
    while size != 0:
        try:
            msg_tmp = sock.recv(size)
        except:
            disconnect_server()
            exit(1)
        size -= msg_tmp.__len__()
        msg += msg_tmp
    return msg


def clientRecv():
    (time_size,) = struct.unpack(">I", saveRecv(4))
    (name_size,) = struct.unpack(">I", saveRecv(4))
    (msg_size,) = struct.unpack(">I", saveRecv(4))
    time = saveRecv(time_size).decode()
    name = saveRecv(name_size).decode()
    msg = saveRecv(msg_size - 1).decode()
    flags = saveRecv(1)[0]
    if flags:
        (path_size,) = struct.unpack(">I", saveRecv(4))
        (file_size,) = struct.unpack(">I", saveRecv(4))
        file_path = saveRecv(path_size).decode()
        file = saveRecv(file_size)

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


def getNonExistentName(file_name):
    parts = file_name.split(".")
    name = parts[0]
    if len(parts) == 1:
        extension = ""
    else:
        extension = "." + parts[1]
    while True:
        path = Path.cwd() / (name + extension)
        if path.exists():
            name += "_new"
        else:
            break
    return name + extension


def clientSend(msg: bytes, path=None):
    flags = 1 * (path is not None)
    msg = bytearray(msg)
    msg.append(flags)
    msg = bytes(msg)
    msg_size = msg.__len__()

    size_packed = struct.pack(">I", msg_size)

    sock.send(size_packed)
    sock.send(msg)
    if path is not None:
        f = open(path, "rb")
        file = f.read()
        f.close()

        file_path = path.encode()
        file_path_size_packed = struct.pack(">I", file_path.__len__())

        file_size_packed = struct.pack(">I", file.__len__())

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
sock.connect(('networkslab-ivt.ftp.sh', 9090))

max_name_len = 32  # limit on client side
while True:
    name = input(f'Enter your name({max_name_len} symbols max):').encode()
    if name.__len__() > max_name_len:
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

    if file_path.__len__() == 0:
        file_path = None
    if diseconnect_event.is_set():
        break
    clientSend(msg.encode(), file_path)

listenThread.join()
sock.close()
