import os.path, socket
import Tools
from WorkMode import WorkMode


class PackageHandler:
    def __init__(self, sock, address, work_mode, file_name):
        self.sock = sock
        self.sock.settimeout(4)
        self.address = address
        self.work_mode = work_mode
        self.file_name = file_name

        self.package = None
        self.current_block = 0

    def get_nonexistent_file_name(self):
        parts = self.file_name.split(".")
        if len(parts) == 1:  # имя файла не содержит "."
            name = self.file_name
            extension = ""
        else:
            name = ".".join(parts[:-1])  # имя файла может содержать несколько "."
            extension = "." + parts[-1]
        while True:
            if os.path.isfile(os.getcwd() + os.sep + name + extension):
                name += "_new"
            else:
                break
        return name + extension

    def send(self, data, wait_next_package=True):
        self.package = None
        next_package = False
        for _ in range(2):  # две попытки отправить пакет
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
        try:
            data, addr = self.sock.recvfrom(1024)
            if self.address != addr:  # на случай, если сервер создаст новый сокет, и адрес сменится
                self.address = addr
            if len(data) != 0:
                self.package = Tools.get_package(data)
                return True
            else:
                return False
        except socket.error:
            return False

    def run(self):
        if not self.send(Tools.create_request_package(self.work_mode.value, self.file_name)): return

        if self.package["opcode"] != 5:  # 5 = err
            if self.work_mode == WorkMode.DOWNLOAD:
                file = open(self.get_nonexistent_file_name(), "wb")
            else:
                file = open(self.file_name, "rb")

        while True:
            if self.work_mode == WorkMode.DOWNLOAD:
                match self.package["opcode"]:
                    case 3:  # 3 = data
                        self.current_block += 1
                        if self.package["current_block"] == self.current_block:
                            file.write(self.package["data"])
                            if len(self.package["data"]) == 512:
                                if self.send(Tools.create_ack_package(self.current_block)):
                                    continue
                                else:  # весь файл загружен
                                    break
                            else:
                                self.send(Tools.create_ack_package(self.current_block), wait_next_package=False)
                                file.close()
                                break
                        else:  # пришел пакет с неправильным номером блока => игнорируем его
                            if self.receive_new_package():
                                continue
                            else:
                                break
                    case 5:  # 5 = err
                        print(str(self.package["err_code"]) + " : " + self.package["err_message"])
                        break
                    case _:
                        break

            if self.work_mode == WorkMode.UPLOAD:
                match self.package["opcode"]:
                    case 4:  # 4 = ack
                        if self.package["current_block"] == self.current_block:
                            self.current_block += 1
                            buffer = file.read(512)
                            if not self.send(Tools.create_data_package(self.current_block, buffer)): break
                            if len(buffer) == 512:
                                continue
                            else:  # весь файл отправлен
                                file.close()
                                break
                        else:  # пришел пакет с неправильным номером блока => игнорируем его
                            if self.receive_new_package():
                                continue
                            else:
                                break
                    case 5:  # 5 = err
                        print(str(self.package["err_code"]) + " : " + self.package["err_message"])
                        break
                    case _:
                        break

        self.sock.close()
