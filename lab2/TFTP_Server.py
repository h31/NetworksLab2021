import socket
from ClientHandler import ClientHandler

sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
sock.bind(("", 1069))

clients = {} # {ip1 : {port1 : clientHandler1, port2 : clientHandler2}, ip2 : {port3 : clientHandler3}}
while True:
	try:
		data, address = sock.recvfrom(1024)
	except socket.error:
		clients[address[0]][address[1]] = None
		continue
	
	ip = address[0]
	port = address[1]
	new_client = False
	if ip not in clients:
		new_client = True
		clients[ip] = {port : ClientHandler(socket.socket(socket.AF_INET, socket.SOCK_DGRAM), address)}
	else:
		if port not in clients[ip]:
			new_client = True
		else:
			if clients[ip][port] == None:
				new_client = True
		if new_client:
			clients[ip][port] = ClientHandler(socket.socket(socket.AF_INET, socket.SOCK_DGRAM), address)
	
	clients[ip][port].new_package(data)
	if new_client: clients[ip][port].start()