import socket, struct, os.path
from Direction import Direction 

class ClientHandler():
	def __init__(self, sock, address, direction = Direction.NOT_SET, filename = None):
		self.sock = sock
		self.address = address 
		self.direction = direction
		self.filename = filename
		self.block_number = 0
		self.package = None # the last received package
	
	def new_package(self, data):
		self.package = self.parse(data)
	
	def parse(self, data):
		result = {}
		result["opcode"] = int.from_bytes(bytes = data[:2], byteorder = "big")
		if result["opcode"] in (1, 2):
			self.filename = data[2:-1].split(b'\x00')[0].decode()
		if result["opcode"] == 3:
			result["block_number"] = int.from_bytes(bytes = data[2:4], byteorder = "big")
			result["data"] = data[4:]
		if result["opcode"] == 4:
			result["block_number"] = int.from_bytes(bytes = data[2:], byteorder = "big")
		if result["opcode"] == 5:
			result["error_code"] = int.from_bytes(bytes = data[2:4], byteorder = "big")
			result["error_message"] = data[4:-1].decode()
		return result
	
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
			if self.receive_new_package(): # new package has been received
				success = True
				break
		return success

	def receive_new_package(self, timeout = 5):
		self.sock.settimeout(timeout)
		success = True
		try:
			data, _ = self.sock.recvfrom(1024)
			if len(data) != 0:
				self.package = self.parse(data)
			else:
				success = False
		except socket.error:
			success = False
		
		self.sock.settimeout(None)
		return success
	
	def run(self):
		if self.package["opcode"] not in (1, 2):
			self.send_error_package(4, "Illegal TFTP operation.")
			return
		
		if self.package["opcode"] == Direction.GET_FROM_SERVER.value:
			if not os.path.isfile(os.getcwd() + os.sep + self.filename): # checking if the file exists
				self.send_error_package(1, "File not found.")
				return
			else:
				self.direction = Direction.GET_FROM_SERVER
				file = open(self.filename, "rb") # opening the file for reading
				self.block_number = 1
				buffer = file.read(512)
				if not self.send_data_package(buffer): return # connection error
				if not len(buffer) == 512:
					file.close()
					return # len(buffer) < 512 => the entire file has been sent
		
		if self.package["opcode"] == Direction.PUT_TO_SERVER.value:
			if os.path.isfile(os.getcwd() + os.sep + self.filename):
				self.send_error_package(6, "File already exists.") # checking if the file exists
				return
			else:
				self.direction = Direction.PUT_TO_SERVER
				file = open(self.filename, "wb") # creating a new file
				self.block_number = 0
				if not self.send_acknowledgement_package(): return # connection error	
		
		while True:
			if self.direction == Direction.GET_FROM_SERVER: # download from server
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
						if self.receive_new_package():
							continue
						else:
							break # connection error
				self.send_error_package(4, "Illegal TFTP operation.")
				break
		
			if self.direction == Direction.PUT_TO_SERVER: # upload to server
				if self.package["opcode"] == 3: # data
					self.block_number += 1
					if self.package["block_number"] == self.block_number:
						file.write(self.package["data"])
						if len(self.package["data"]) == 512:
							if self.send_acknowledgement_package(): 
								continue
							else:
								break # connection error
						else: # len(self.package["data"]) < 512	=> the entire file was received
							self.send_acknowledgement_package(get_new_package = False)
							file.close()
							break
					else: # drop package
						if self.receive_new_package():
							continue
						else:
							break # connection error
				self.send_error_package(4, "Illegal TFTP operation.")
				break