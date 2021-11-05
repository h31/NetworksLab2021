import socket, sys, os.path
from PackageHandler import PackageHandler

def printHelp():
	print("Args:\t[-i] host [GET | PUT] source")
	print()
	print("-i\tbinary image transfer mode")
	print("host\tlocal or remote host")
	print("GET\tdownload file from server")
	print("PUT\tupload file to server")
	print("source\tfilename")
	exit()

PORT = 69
if len(sys.argv) == 4:
	transfer_mode = "netascii"
	address = (sys.argv[1], PORT)
	cmd = sys.argv[2].upper()
	filename = sys.argv[3]
else:
	if len(sys.argv) == 5:
		if sys.argv[1] == "-i":
			transfer_mode = "octet"
		else:
			printHelp()
		address = (sys.argv[2], PORT)
		cmd = sys.argv[3].upper()
		filename = sys.argv[4]
	else:
		printHelp()

if cmd not in ("GET", "PUT"): printHelp()
if cmd == "GET":
	work_mode = 1 # 1 = GET (download from server)
else: # "PUT"
	if os.path.isfile(os.getcwd() + os.sep + filename):
		work_mode = 2 # 2 = PUT (upload to server)
	else:
		print("File not found")
		exit()

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.connect(address)
handler = PackageHandler(sock, address, work_mode, filename, transfer_mode)
handler.start()
while True:
	try:
		data, _ = sock.recvfrom(1024)
		handler.new_package(data)
	except socket.error:
		break