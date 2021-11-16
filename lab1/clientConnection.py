import struct
from datetime import datetime
from pathlib import Path

import colorama
from colorama import Fore, Style


class Connection:

    def __init__(self, sock, disconnect_event):
        self.sock = sock
        self.disconnect_event = disconnect_event

    def registration(self, username):
        self.sendToServer(username)
        message_time, message_username, reg_result, message_file_path = self.recvFromServer()
        while message_username != 'Server':
            message_time, message_username, reg_result, message_file_path = self.recvFromServer()
        if reg_result == f'False':
            return False
        else:
            return True

    def messageRecv(self, size: int):
        message = bytes()
        while size != 0:
            try:
                message_tmp = self.sock.recv(size)
            except ConnectionResetError:
                print('Сервер отключился :(\nНажмите Enter, чтобы выйти')
                self.disconnect_event.set()
                exit(1)
            size -= len(message_tmp)
            message += message_tmp
        return message

    def recvFromServer(self):
        (time_size,) = struct.unpack(">I", self.messageRecv(4))
        (username_size,) = struct.unpack(">I", self.messageRecv(4))
        (message_size,) = struct.unpack(">I", self.messageRecv(4))
        time = self.messageRecv(time_size).decode()
        username = self.messageRecv(username_size).decode()
        message = self.messageRecv(message_size - 1).decode()
        if self.messageRecv(1)[0] == 1:
            contain_file = True
        else:
            contain_file = False
        if contain_file:
            (path_size,) = struct.unpack(">I", self.messageRecv(4))
            (file_size,) = struct.unpack(">I", self.messageRecv(4))
            file_path = self.messageRecv(path_size).decode().split('\\')
            path = str(Path.cwd()) + '\\' + file_path[len(file_path)-1]
            file = self.messageRecv(file_size)
            new_path = self.checkFileName(path)
            f = open(new_path, "w+b")
            f.write(file)
            f.close()
        else:
            new_path = None
        return time, username, message, new_path

    def printMessage(self, time, username_message, message, file_path):
        time = self.getTime(time)
        if file_path is not None:
            text = f' (Вам был отправлен файл - {file_path} )'
        else:
            text = ""
        if username_message == 'Server':
            colorama.init()
            print(Fore.GREEN + f'\n<{time}>[{username_message}] {message}{text}')
            print(Style.RESET_ALL)
        else:
            print(f'\n<{time}>[{username_message}] {message}{text}')

    def recvAndPrint(self):
        while True:
            message_time, message_name, message, file_path = self.recvFromServer()
            self.printMessage(message_time, message_name, message, file_path)

    def checkFileName(self, file_name):
        parts = file_name.split(".")
        name = parts[0]
        if len(parts) > 2:
            for i in range(1, len(parts) - 1):
                name += "." + parts[i]
        while True:
            file_name = name + '.' + parts[len(parts) - 1]
            path = Path.cwd() / file_name
            if path.exists():
                name += "(from_chat)"
            else:
                break
        return file_name

    def sendToServer(self, message: bytes, path=None):
        message = bytearray(message)
        if path is not None:
            contain_file = True
        else:
            contain_file = False
        message.append(contain_file)
        size_packed = struct.pack(">I", len(bytes(message)))
        self.sock.send(size_packed)
        self.sock.send(message)
        if contain_file:
            with open(path, "rb") as f:
                file = f.read()
            file_path = path.encode()
            file_path_size = struct.pack(">I", len(file_path))
            file_size = struct.pack(">I", len(file))
            self.sock.send(file_path_size)
            self.sock.send(file_size)
            self.sock.send(file_path)
            self.sock.send(file)

    def getTime(self, utcTime):
        return str(datetime.fromisoformat(utcTime).astimezone().strftime('%H:%M'))
