import asyncio
import struct

from datetime import datetime, timezone

clientList = ['Server']


class ClientThread:
    def __init__(self, sock, address, threads):
        self.sock = sock
        self.address = address
        self.threads = threads
        self.username = ""

    async def registration(self):
        global clientList
        (time, username, message, contain_file, file_path) = await self.recvFromClient()
        self.username = message.decode()
        if self.username not in clientList:
            clientList.append(self.username)
            return True
        else:
            return False

    async def messageRecv(self, size: int):
        message = bytes()
        loop = asyncio.get_event_loop()
        while size != 0:
            message_tmp = await loop.sock_recv(self.sock, size)
            size -= len(message_tmp)
            message += message_tmp
        return message

    async def recvFromClient(self):
        (size,) = struct.unpack(">I", await self.messageRecv(4))
        message = await self.messageRecv(size - 1)
        recv_flag = await self.messageRecv(1)
        if recv_flag[0] == 1:
            contain_file = True
        else:
            contain_file = False
        file_path_decode = None
        if contain_file:
            (path_size,) = struct.unpack(">I", await self.messageRecv(4))
            (file_size,) = struct.unpack(">I", await self.messageRecv(4))
            file_path_decode = await self.messageRecv(path_size)
            file_path_decode = file_path_decode.decode()
            file_path = await self.messageRecv(file_size)
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

    async def sendToEveryone(self, message_all: bytes):
        loop = asyncio.get_event_loop()
        for thread in self.threads:
            if thread.address != self.address and thread.username in clientList:
                await loop.sock_sendall(thread.sock, message_all)

    async def sendToMyself(self, message_all: bytes):
        loop = asyncio.get_event_loop()
        for thread in self.threads:
            if thread.address == self.address:
                await loop.sock_sendall(thread.sock, message_all)

    def serverMessage(self, text):
        return self.createMessage(
            str(datetime.now(tz=timezone.utc)), "Server", text.encode(), False)

    async def run(self):
        success = await self.registration()
        while not success:
            message = self.serverMessage(f'False')
            await self.sendToMyself(message)
            success = await self.registration()
        if success:
            message = self.serverMessage(f'True')
            await self.sendToMyself(message)
        connect_message = self.serverMessage(f'{self.username} теперь в чате')
        await self.sendToEveryone(connect_message)

        while True:
            global clientList
            try:

                (time, username, message, contain_file, file_path) = await self.recvFromClient()
                if message.decode() == 'q':
                    clientList.remove(self.username)
                    create_message = self.serverMessage(f'{self.username} отключился')
                    disconnect_message = self.serverMessage(f'Отключение')
                    await self.sendToMyself(disconnect_message)
                    await self.sendToEveryone(create_message)
                    break
                else:
                    create_message = self.createMessage(time, username, message, contain_file, file_path)
                await self.sendToEveryone(create_message)
            except ConnectionResetError:
                print(self.username, 'отключился')
                clientList.remove(self.username)
                create_message = self.serverMessage(f'{self.username} отключился')
                await self.sendToEveryone(create_message)
                break



