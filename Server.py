import socket
from ClientHandler import ClientHandler

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(("", 1269))

while True:
	try:
		data, address = sock.recvfrom(1024)
	except socket.error:
		break
	ClientHandler(sock, address, data).run()

sock.close()