import socket, sys, os.path, struct
from Direction import Direction

def parse(data):
	global filename
	
	result = {}
	result["opcode"] = int.from_bytes(bytes = data[:2], byteorder = "big")
	if result["opcode"] in (1, 2):
		filename = data[2:-1].split(b'\x00')[0].decode()
	if result["opcode"] == 3:
		result["block_number"] = int.from_bytes(bytes = data[2:4], byteorder = "big")
		result["data"] = data[4:]
	if result["opcode"] == 4:
		result["block_number"] = int.from_bytes(bytes = data[2:], byteorder = "big")
	if result["opcode"] == 5:
		result["error_code"] = int.from_bytes(bytes = data[2:4], byteorder = "big")
		result["error_message"] = data[4:-1].decode()
	return result

def send_request_package():
	global direction
	global filename
	
	package = struct.pack(">h", direction.value) # h = 2 bytes
	package += filename.encode()
	package += struct.pack(">c", b'\x00') # c = 1 byte
	package += "octet".encode()
	package += struct.pack(">c", b'\x00') # c = 1 byte
	return send(package, get_new_package = True)

def send_data_package(data):
	global block_number
	
	if not (block_number > 0 and block_number < 65536 and #2^16
		len(data) <= 512): return False # error
	package = struct.pack(">h", 3) # h = 2 bytes
	package += struct.pack(">h", block_number) # h = 2 bytes
	package += data
	return send(package, get_new_package = True)

def send_acknowledgement_package(get_new_package = True):
	global block_number
	
	if not (block_number >= 0 and block_number < 65536): return False # error
	package = struct.pack(">h", 4) # h = 2 bytes
	package += struct.pack(">h", block_number) # h = 2 bytes
	return send(package, get_new_package)

def send_error_package(error_code, error_message):
	package = struct.pack(">h", 5) # h = 2 bytes
	package += struct.pack(">h", error_code) # h = 2 bytes
	package += error_message.encode()
	package += struct.pack(">c", b'\x00') # c = 1 byte
	return send(package, get_new_package = False)

def send(data, get_new_package = True):
	global sock
	global package
	global address
	
	package = None
	success = False
	for _ in range(4): # 4 attempts to send package
		try:
			sock.sendto(data, address)
		except socket.error:
			break
		if not get_new_package: break
		if receive_new_package(): # new package has been received
			success = True
			break
	return success

def receive_new_package(timeout = 5):
	global sock
	global package
	global address
	
	sock.settimeout(timeout)
	try:
		data, addr = sock.recvfrom(1024)
		address = addr
		if len(data) != 0:
			package = parse(data)
			return True
		else:
			return False
	except socket.error:
		return False

def getNonExistentFilename():
	global filename
	parts = filename.split(".")
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

def run():
	global direction
	global filename
	global block_number
	block_number = 0
	
	if not send_request_package(): return # connection error
	
	if package["opcode"] != 5: # 5 = error
		if direction == Direction.GET_FROM_SERVER:
			file = open(getNonExistentFilename(), "wb")
		else: # PUT_TO_SERVER (upload to server)
			file = open(filename, "rb")

	while True:
		if direction == Direction.GET_FROM_SERVER:
			if package["opcode"] == 3: # data package
				block_number += 1
				if package["block_number"] == block_number:
					file.write(package["data"])
					if len(package["data"]) == 512:
						if send_acknowledgement_package(): 
							continue
						else:
							break # connection error
					else:
						send_acknowledgement_package(get_new_package = False)
						file.close()
						break # package_length < 512 => the entire file was received
				else: # drop package
					if receive_new_package():
						continue
					else:
						break # connection error
			if package["opcode"] == 5: # error
				print("Error " + str(package["error_code"]) + ": " + package["error_message"])
				break
			# package["opcode"] not in (3, 5)
			send_error_package(4, "Illegal TFTP operation.")
			break                
				
		if direction == Direction.PUT_TO_SERVER:
			if package["opcode"] == 4: # acknowledgement package
				if package["block_number"] == block_number:
					block_number += 1
					buffer = file.read(512)
					if not send_data_package(buffer): break # connection error
					if len(buffer) == 512:
						continue
					else:
						file.close()
						break # len(buffer) < 512 => the entire file has been sent
				else: # drop package
					if receive_new_package():
						continue
					else:
						break # connection error
			if package["opcode"] == 5: # error
				print("Error " + str(package["error_code"]) + ": " + package["error_message"])
				break
			# package["opcode"] not in (4, 5)
			send_error_package(4, "Illegal TFTP operation.")
			break  

def printHelp():
	print("Args:\thost [GET | PUT] source")
	print()
	print("host\tlocal or remote host")
	print("GET\tdownload file from server")
	print("PUT\tupload file to server")
	print("source\tfilename")
	exit()

if len(sys.argv) != 4: printHelp()
address = (sys.argv[1], 1069)
cmd = sys.argv[2].upper()
filename = sys.argv[3]
if cmd not in ("GET", "PUT"): printHelp()
if cmd == "GET":
	direction = Direction.GET_FROM_SERVER # download from server
else: # "PUT"
	if os.path.isfile(os.getcwd() + os.sep + filename):
		direction = Direction.PUT_TO_SERVER # upload to server
	else:
		print("File not found")
		exit()

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
run()
sock.close()