import socket, os.path
import Tools
from WorkMode import WorkMode


class ClientHandler():
    def __init__(self, sock, address, first_package):
        self.sock = sock
        self.address = address
        self.package = Tools.get_package(first_package)

        self.work_mode = None
        self.file = None
        self.current_block = 0

    def send(self, data, wait_next_package=True):
        self.package = None
        next_package = False
        for _ in range(2):
            try:
                self.sock.sendto(data, self.address)
            except socket.error:
                break
            if not wait_next_package: break
            if self.receive_new_package():
                next_package = True
                break
        return next_package

    def receive_new_package(self):
        self.sock.settimeout(4)
        next_package = True
        try:
            data, _ = self.sock.recvfrom(1024)
            if len(data) != 0:
                self.package = Tools.get_package(data)
            else:
                next_package = False
        except socket.error:
            next_package = False

        self.sock.settimeout(None)
        return next_package

    def initial_package_handler(self):
        match self.package["opcode"]:
            case WorkMode.DOWNLOAD.value:
                if not os.path.isfile(os.getcwd() + os.sep + self.package["file_name"]):
                    self.send(Tools.create_err_package(1, "File not found."), wait_next_package=False)
                    return False
                else:
                    self.work_mode = WorkMode.DOWNLOAD
                    self.file = open(self.package["file_name"], "rb")
                    self.current_block = 1
                    buffer = self.file.read(512)
                    if not self.send(Tools.create_data_package(self.current_block, buffer)): return False
                    if len(buffer) != 512:
                        self.file.close()
                        return False
                    return True
            case WorkMode.UPLOAD.value:
                if os.path.isfile(os.getcwd() + os.sep + self.package["file_name"]):
                    self.send(Tools.create_err_package(6, "File already exists."), wait_next_package=False)
                    return False
                else:
                    self.work_mode = WorkMode.UPLOAD
                    self.file = open(self.package["file_name"], "wb")
                    self.current_block = 0
                    if not self.send(Tools.create_ack_package(self.current_block)): return False
                    return True
            case _:
                self.send(Tools.create_err_package(4, "Illegal TFTP operation."), wait_next_package=False)
                return False

    def run(self):
        if not self.initial_package_handler():
            return
        while True:
            if self.work_mode == WorkMode.DOWNLOAD:
                if self.package["opcode"] == 4:  # 4 = ack
                    if self.package["current_block"] == self.current_block:
                        self.current_block += 1
                        buffer = self.file.read(512)
                        if not self.send(Tools.create_data_package(self.current_block, buffer)): break
                        if len(buffer) == 512:
                            continue
                        else:
                            self.file.close()
                            break
                    else:
                        if self.receive_new_package():
                            continue
                        else:
                            break
                else:
                    self.send(Tools.create_err_package(4, "Illegal TFTP operation."), wait_next_package=False)
                    break

            if self.work_mode == WorkMode.UPLOAD:
                if self.package["opcode"] == 3:  # 3 = data
                    self.current_block += 1
                    if self.package["current_block"] == self.current_block:
                        self.file.write(self.package["data"])
                        if len(self.package["data"]) == 512:
                            if self.send(Tools.create_ack_package(self.current_block)):
                                continue
                            else:
                                break
                        else:
                            self.send(Tools.create_ack_package(self.current_block), wait_next_package=False)
                            self.file.close()
                            break
                    else:
                        if self.receive_new_package():
                            continue
                        else:
                            break
                else:
                    self.send(Tools.create_err_package(4, "Illegal TFTP operation."), wait_next_package=False)
                    break
