import struct
import threading

from datetime import datetime, timezone

clientList = ['Server']


class ClientThread(threading.Thread):
    def __init__(self, sock, address, threads):
        threading.Thread.__init__(self)
        self.sock = sock
        self.address = address
        self.threads = threads
        self.username = ""

    def registration(self):
        global clientList
        (time, username, message, contain_file, file_path) = self.recvFromClient()
        self.username = message.decode()
        if self.username not in clientList:
            clientList.append(self.username)
            return True
        else:
            return False

    def messageRecv(self, size: int):
        message = bytes()
        while size != 0:
            message_tmp = self.sock.recv(size)
            size -= len(message_tmp)
            message += message_tmp
            # except ConnectionResetError:
            #     print(self.username, 'отключился')
            #     global clientList
            #     clientList.remove(self.username)
            #     create_message = self.serverMessage(f'{self.username} отключился')
            #     self.sendToEveryone(create_message)
            #     break
        return message

    def recvFromClient(self):
        (size,) = struct.unpack(">I", self.messageRecv(4))
        message = self.messageRecv(size - 1)
        if self.messageRecv(1)[0] == 1:
            contain_file = True
        else:
            contain_file = False
        file_path_decode = None
        if contain_file:
            (path_size,) = struct.unpack(">I", self.messageRecv(4))
            (file_size,) = struct.unpack(">I", self.messageRecv(4))
            file_path_decode = self.messageRecv(path_size).decode()
            file_path = self.messageRecv(file_size)
            with open(file_path_decode, "wb") as f:
                f.write(file_path)
        time = str(datetime.now(tz=timezone.utc))
        username = self.username
        return time, username, message, contain_file, file_path_decode

    def createMessage(self, time: str, username: str, message: bytes, contain_file: bool, path=None):
        time_size = struct.pack(">I", len(time.encode()))
        username_size = struct.pack(">I", len(username.encode()))
        message_size = struct.pack(">I", len(message) + 1)

        flag_encode = bytearray()
        flag_encode.append(contain_file)
        flag_encode = bytes(flag_encode)

        file = bytes()
        file_size = bytes()
        file_path = bytes()
        file_path_size = bytes()
        if contain_file:
            f = open(path, "rb")
            file = f.read()
            f.close()
            file_path = path.encode()
            file_path_size = struct.pack(">I", len(file_path))
            file_size = struct.pack(">I", len(file))

        message_all = bytes([*time_size, *username_size, *message_size, *time.encode(),
                             *username.encode(), *message, *flag_encode, *file_path_size,
                             *file_size, *file_path, *file])
        return message_all

    def sendToEveryone(self, message_all: bytes):
        for thread in self.threads:
            if thread.address != self.address and thread.username in clientList:
                thread.sock.send(message_all)

    def sendToMyself(self, message_all: bytes):
        for thread in self.threads:
            if thread.address == self.address:
                thread.sock.send(message_all)

    def serverMessage(self, text):
        return self.createMessage(
            str(datetime.now(tz=timezone.utc)), "Server", text.encode(), False)

    def run(self):
        success = self.registration()
        while not success:
            message = self.serverMessage(f'False')
            self.sendToMyself(message)
            success = self.registration()
        if success:
            message = self.serverMessage(f'True')
            self.sendToMyself(message)
        connect_message = self.serverMessage(f'{self.username} теперь в чате')
        self.sendToEveryone(connect_message)

        while True:
            global clientList
            try:
                (time, username, message, contain_file, file_path) = self.recvFromClient()
                if message.decode() == 'q':
                    clientList.remove(self.username)
                    create_message = self.serverMessage(f'{self.username} отключился')
                    disconnect_message = self.serverMessage(f'Отключение')
                    self.sendToMyself(disconnect_message)
                    self.sendToEveryone(create_message)
                    break
                else:
                    create_message = self.createMessage(time, username, message, contain_file, file_path)
                self.sendToEveryone(create_message)
            except ConnectionResetError:
                print(self.username, 'отключился')
                clientList.remove(self.username)
                create_message = self.serverMessage(f'{self.username} отключился')
                self.sendToEveryone(create_message)
                break
