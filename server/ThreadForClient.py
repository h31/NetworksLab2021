import struct
import threading
from datetime import datetime, timezone
from pathlib import Path


class ThreadForClient(threading.Thread):
    def __init__(self, sock, addr, threads):
        threading.Thread.__init__(self)
        self.sock = sock
        self.addr = addr
        self.threads = threads
        self.name = ""

    def reg(self):
        (time, name, msg, flags, path) = self.serverRecv()
        self.name = msg.decode()

    def saveRecv(self, size: int):
        msg = bytes()
        while size != 0:
            msg_tmp = self.sock.recv(size)
            size -= msg_tmp.__len__()
            msg += msg_tmp
        return msg

    def serverRecv(self):
        (size,) = struct.unpack(">I", self.saveRecv(4))
        msg = self.saveRecv(size - 1)
        flags = self.saveRecv(1)[0]
        # print(flags)
        path = None
        actual_path = None
        if flags:
            # print("flag set")
            path_size_p = self.saveRecv(4)
            (path_size_up,) = struct.unpack(">I", path_size_p)

            file_size_p = self.saveRecv(4)
            (file_size_up,) = struct.unpack(">I", file_size_p)

            path = self.saveRecv(path_size_up).decode()
            file = self.saveRecv(file_size_up)
            print(type(path))
            actual_path = self.getNonExistentName(path)

            f = open(actual_path, "w+b")
            f.write(file)
            f.close()
        time = str(datetime.now(tz=timezone.utc))
        name = self.name
        return time, name, msg, flags, actual_path

    def createMsg(self, time: str, name: str, msg: bytes, flags: int, path=None):
        time_encode = time.encode()
        time_size = time_encode.__len__()
        time_size_p = struct.pack(">I", time_size)

        name_encode = name.encode()
        name_size = name_encode.__len__()
        name_size_p = struct.pack(">I", name_size)

        msg_size = msg.__len__() + 1
        msg_size_p = struct.pack(">I", msg_size)

        flags_encode = bytearray()
        flags_encode.append(flags)
        flags_encode = bytes(flags_encode)

        file = bytes()
        file_size_p = bytes()
        file_path = bytes()
        file_path_size_p = bytes()
        if path is not None:
            f = open(path, "rb")
            file = f.read()
            f.close()

            file_path = path.encode()
            file_path_size_p = struct.pack(">I", file_path.__len__())

            file_size_p = struct.pack(">I", file.__len__())

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

    # def serverSend(self, sock, msg_all):
    #     sock.send(msg_all)

    def sendToEveryone(self, msg_all: bytes):
        for thread in self.threads:
            if thread.addr != self.addr:
                thread.sock.send(msg_all)

    def server_msg(self, text):
        return self.createMsg(
            str(datetime.now(tz=timezone.utc)),
            "Server",
            text.encode(),
            0
        )

    def getNonExistentName(self, file_name):
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

    def run(self):
        # print(f'Connect {self.addr}')
        self.reg()
        connect_msg = self.server_msg(f'{self.name} connected')
        #     self.createMsg(
        #     str(datetime.now(tz=timezone.utc)),
        #     "Server",
        #     f'{self.name} connected'.encode(),
        #     0
        # )
        self.sendToEveryone(connect_msg)
        while True:
            recv_msg = self.serverRecv()
            crt_msg = self.createMsg(*recv_msg)
            # print(crt_msg)
            self.sendToEveryone(crt_msg)
