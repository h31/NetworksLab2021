import socket, time, struct, os.path, threading

class PackageHandler(threading.Thread):
    def __init__(self, sock, address, work_mode = None, filename = None, transfer_mode = None):
        threading.Thread.__init__(self)
        self.sock = sock
        self.address = address 
        self.work_mode = work_mode # None = not set; 1 = GET (download from server); 2 = PUT (upload to server)
        self.filename = filename
        self.transfer_mode = transfer_mode # None = not set; octet; netascii
        self.block_number = 0
        self.package = None # the last received package
    
    def new_package(self, data):
        self.package = self.parse(data)
    
    def parse(self, data):
        result = {}
        opcode = int.from_bytes(bytes = data[:2], byteorder = "big")
        result["opcode"] = opcode
        if opcode in (1, 2):
            parts = data[2:-1].split(b'\x00')
            result["filename"] = parts[0].decode()
            self.transfer_mode = parts[1].decode()
        
        if opcode == 3:
            result["block_number"] = int.from_bytes(bytes = data[2:4], byteorder = "big")
            if self.transfer_mode == "octet":
                result["data"] = data[4:]
            else: # netascii
                result["data"] = data[4:].decode()

        if opcode == 4:
            result["block_number"] = int.from_bytes(bytes = data[2:], byteorder = "big")
        
        if opcode == 5:
            result["error_code"] = int.from_bytes(bytes = data[2:4], byteorder = "big")
            result["error_message"] = data[4:-1].decode()
        
        return result
    
    def send_request_package(self):
        package = struct.pack(">h", self.work_mode) # h = 2 bytes
        package += self.filename.encode()
        package += struct.pack(">c", b'\x00') # c = 1 byte
        package += self.transfer_mode.encode()
        package += struct.pack(">c", b'\x00') # c = 1 byte
        return self.send(package, get_new_package = True)
    
    def send_data_package(self, data):
        if not (self.block_number > 0 and self.block_number < 65536 and #2^16
            len(data) <= 512): return False # error
        package = struct.pack(">h", 3) # h = 2 bytes
        package += struct.pack(">h", self.block_number) # h = 2 bytes
        package += data
        return self.send(package, get_new_package = True)
    
    def send_acknowledgement_package(self, get_new_package = True):
        if not (self.block_number >= 0 and self.block_number < 65536): return False # error
        package = struct.pack(">h", 4) # h = 2 bytes
        package += struct.pack(">h", self.block_number) # h = 2 bytes
        return self.send(package, get_new_package)
    
    def send_error_package(self, error_code, error_message):
        package = struct.pack(">h", 5) # h = 2 bytes
        package += struct.pack(">h", error_code) # h = 2 bytes
        package += error_message.encode()
        package += struct.pack(">c", b'\x00') # c = 1 byte
        return self.send(package, get_new_package = False)
    
    def send(self, data, get_new_package = True):
        self.package = None
        success = False
        for _ in range(4): # 4 attempts to send package
            try:
                self.sock.sendto(data, self.address)
            except socket.error:
                break
            if not get_new_package: break
            if self.wait_new_package(): # new package has been received
                success = True
                break
        return success

    def wait_new_package(self, timeout = 2):
        delay = 0.1
        while timeout > 0:
            time.sleep(delay)
            if self.package == None: 
                timeout -= delay
            else: # new package has been received
                return True
        return False

    def getNonExistentFilename(self):
        parts = self.filename.split(".")
        name = parts[0]
        if len(parts) == 1:
            extension = ""
        else:
            extension = "." + parts[1]
        while True:
            if os.path.isfile(os.getcwd() + os.sep + name + extension):
                name += "_new"
            else:
                break
        return name + extension

    def client_part(self):
        if not self.send_request_package(): return # connection error
        
        if self.package["opcode"] != 5: # 5 = error
            if self.work_mode == 1: # 1 = GET (download from server)
                if self.transfer_mode == "octet":
                    file = open(self.getNonExistentFilename(), "wb")
                else: # netascii
                    file = open(self.getNonExistentFilename(), "w")
            else: # 2 = PUT (upload to server)
                file = open(self.filename, "rb")
    
        while True:
            if self.work_mode == 1: # 1 = GET (download from server)
                if self.package["opcode"] == 3: # data
                    self.block_number += 1
                    if self.package["block_number"] == self.block_number:
                        file.write(self.package["data"])
                        if len(self.package["data"]) == 512:
                            if self.send_acknowledgement_package(): 
                                continue
                            else:
                                break # connection error
                        else: # len(self.package["data"]) < 512 => the entire file was received
                            self.send_acknowledgement_package(get_new_package = False)
                            file.close()
                            break # package_length < 512 => the entire file was received
                    else: # drop package
                        if self.wait_new_package(timeout = 4):
                            continue
                        else:
                            break # connection error
                if self.package["opcode"] == 5: # error
                    print("Error " + str(self.package["error_code"]) + ": " + self.package["error_message"])
                    break
                # self.package["opcode"] not in (3, 5)
                self.send_error_package(4, "Illegal TFTP operation.")
                break                
                    
            if self.work_mode == 2: # 2 = PUT (upload to server)
                if self.package["opcode"] == 4: # acknowledgement
                    if self.package["block_number"] == self.block_number:
                        self.block_number += 1
                        buffer = file.read(512)
                        if not self.send_data_package(buffer): break # connection error
                        if len(buffer) == 512:
                            continue
                        else:
                            file.close()
                            break # len(buffer) < 512 => the entire file has been sent
                    else: # drop package
                        if self.wait_new_package(timeout = 4):
                            continue
                        else:
                            break # connection error
                if self.package["opcode"] == 5: # error
                    print("Error " + str(self.package["error_code"]) + ": " + self.package["error_message"])
                    break
                # self.package["opcode"] not in (4, 5)
                self.send_error_package(4, "Illegal TFTP operation.")
                break    
    
    def server_part(self):
        if self.package["opcode"] not in (1, 2):
            self.send_error_package(4, "Illegal TFTP operation.")
            return
        
        if self.package["opcode"] == 1: # 1 = GET (download from server)
            self.work_mode = 1
            if not os.path.isfile(os.getcwd() + os.sep + self.package["filename"]):
                self.send_error_package(1, "File not found.")
                return
            else:
                file = open(self.package["filename"], "rb")
                self.block_number = 1
                buffer = file.read(512)
                if not self.send_data_package(buffer): return # connection error
                if not len(buffer) == 512:
                    return # len(buffer) < 512 => the entire file has been sent
        
        if self.package["opcode"] == 2: # 2 = PUT (upload to server)
            self.work_mode = 2
            if os.path.isfile(os.getcwd() + os.sep + self.package["filename"]):
                self.send_error_package(6, "File already exists.")
                return
            else:
                if self.transfer_mode == "octet":
                    file = open(self.package["filename"], "wb")
                else: # netascii
                    file = open(self.package["filename"], "w")
                self.block_number = 0
                if not self.send_acknowledgement_package(): return # connection error    
        
        while True:
            if self.work_mode == 1: # 1 = GET (download from server)
                if self.package["opcode"] == 4: # acknowledgement
                    if self.package["block_number"] == self.block_number:
                        self.block_number += 1
                        buffer = file.read(512)
                        if not self.send_data_package(buffer): break # connection error
                        if len(buffer) == 512:
                            continue
                        else:
                            file.close()
                            break # len(buffer) < 512 => the entire file has been sent
                    else: # drop package
                        if self.wait_new_package(timeout = 4):
                            continue
                        else:
                            break # connection error
                if self.package["opcode"] == 5: # error
                    print("Error " + str(self.package["error_code"]) + ": " + str(self.package["error_message"]))
                    break
                # self.package["opcode"] not in (4, 5)
                self.send_error_package(4, "Illegal TFTP operation.")
                break
        
            if self.work_mode == 2: # 2 = PUT (upload to server)
                if self.package["opcode"] == 3: # data
                    self.block_number += 1
                    if self.package["block_number"] == self.block_number:
                        file.write(self.package["data"])
                        if len(self.package["data"]) == 512:
                            if self.send_acknowledgement_package(): 
                                continue
                            else:
                                break # connection error
                        else: # len(self.package["data"]) < 512    => the entire file was received
                            self.send_acknowledgement_package(get_new_package = False)
                            file.close()
                            break
                    else: # drop package
                        if self.wait_new_package(timeout = 4):
                            continue
                        else:
                            break # connection error
                if self.package["opcode"] == 5: # error
                    print("Error " + str(self.package["error_code"]) + ": " + str(self.package["error_message"]))
                    break
                # self.package["opcode"] not in (3, 5)
                self.send_error_package(4, "Illegal TFTP operation.")
                break

    def run(self):
        if self.work_mode == None:
            self.server_part()
        else:
            self.client_part()
            self.sock.close()