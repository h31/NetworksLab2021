import socket
from ClientHandler import ClientHandler

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(("", 1069))

while True:
	try:
		data, address = sock.recvfrom(1024)
	except socket.error:
		break
	
	handler = ClientHandler(sock, address)
	handler.new_package(data)
	handler.run()