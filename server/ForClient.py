import asyncio
import struct
from datetime import datetime, timezone
import sys

sys.path.append('..')
from tools import getNonExistentName


class ForClient():
    def __init__(self, sock, addr, handlers):
        self.sock = sock
        self.addr = addr
        self.handlers = handlers
        self.name = ""

    async def reg(self):
        (time, name, msg, flags, path, file) = await self.serverRecv()
        self.name = msg.decode()

    async def safeRecv(self, size: int):
        msg = bytes()
        loop = asyncio.get_event_loop()
        while size != 0:
            msg_tmp = await loop.sock_recv(self.sock, size)
            size -= len(msg_tmp)
            msg += msg_tmp
        return msg

    async def serverRecv(self):
        (size,) = struct.unpack(">I", await self.safeRecv(4))
        msg = await self.safeRecv(size - 1)
        flags = await self.safeRecv(1)
        flags = flags[0]
        path = None
        actual_path = None
        file = None
        if flags:
            path_size_p = await self.safeRecv(4)
            (path_size_up,) = struct.unpack(">I", path_size_p)

            file_size_p = await self.safeRecv(4)
            (file_size_up,) = struct.unpack(">I", file_size_p)

            path = await self.safeRecv(path_size_up)
            path = path.decode()
            file = await self.safeRecv(file_size_up)
            actual_path = getNonExistentName(path)

            f = open(actual_path, "w+b")
            f.write(file)
            f.close()
        time = str(datetime.now(tz=timezone.utc))
        name = self.name
        return time, name, msg, flags, actual_path, file

    def createMsg(self, time: str, name: str, msg: bytes, flags: int, path=None, file=None):
        time_encode = time.encode()
        time_size = len(time_encode)
        time_size_p = struct.pack(">I", time_size)

        name_encode = name.encode()
        name_size = len(name_encode)
        name_size_p = struct.pack(">I", name_size)

        msg_size = len(msg) + 1
        msg_size_p = struct.pack(">I", msg_size)

        flags_encode = bytearray()
        flags_encode.append(flags)
        flags_encode = bytes(flags_encode)

        file_size_p = bytes()
        file_path = bytes()
        file_path_size_p = bytes()
        if path is not None:
            file_path = path.encode()
            file_path_size_p = struct.pack(">I", len(file_path))
            file_size_p = struct.pack(">I", len(file))
        else:
            file = bytes()

        c = [
            *time_size_p,
            *name_size_p,
            *msg_size_p,
            *time_encode,
            *name_encode,
            *msg,
            *flags_encode,
            *file_path_size_p,
            *file_size_p,
            *file_path,
            *file
        ]
        msg_all = bytes(c)
        return msg_all

    async def sendToEveryone(self, msg_all: bytes):
        loop = asyncio.get_event_loop()
        for handler in self.handlers:
            if handler.addr != self.addr:
                await loop.sock_sendall(handler.sock, msg_all)

    def server_msg(self, text):
        return self.createMsg(
            str(datetime.now(tz=timezone.utc)),
            "Server",
            text.encode(),
            0
        )

    async def run(self):
        await self.reg()
        connect_msg = self.server_msg(f'{self.name} connected')
        await self.sendToEveryone(connect_msg)
        while True:
            recv_msg = await self.serverRecv()
            crt_msg = self.createMsg(*recv_msg)
            await self.sendToEveryone(crt_msg)
