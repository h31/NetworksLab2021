import struct
import threading
from datetime import datetime, timezone
import sys
sys.path.append('..')
from tools import getNonExistentName


class ThreadForClient(threading.Thread):
    def __init__(self, sock, addr, threads):
        threading.Thread.__init__(self)
        self.sock = sock
        self.addr = addr
        self.threads = threads
        self.name = ""

    def reg(self):
        (time, name, msg, flags, path, file) = self.serverRecv()
        self.name = msg.decode()

    def saveRecv(self, size: int):
        msg = bytes()
        while size != 0:
            msg_tmp = self.sock.recv(size)
            size -= len(msg_tmp)
            msg += msg_tmp
        return msg

    def serverRecv(self):
        (size,) = struct.unpack(">I", self.saveRecv(4))
        msg = self.saveRecv(size - 1)
        flags = self.saveRecv(1)[0]
        path = None
        actual_path = None
        file = None
        if flags:
            path_size_p = self.saveRecv(4)
            (path_size_up,) = struct.unpack(">I", path_size_p)

            file_size_p = self.saveRecv(4)
            (file_size_up,) = struct.unpack(">I", file_size_p)

            path = self.saveRecv(path_size_up).decode()
            file = self.saveRecv(file_size_up)
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

    def run(self):
        self.reg()
        connect_msg = self.server_msg(f'{self.name} connected')
        self.sendToEveryone(connect_msg)
        while True:
            recv_msg = self.serverRecv()
            crt_msg = self.createMsg(*recv_msg)
            self.sendToEveryone(crt_msg)
