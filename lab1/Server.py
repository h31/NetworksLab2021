import socket
from NewClientThread import NewClientThread

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("", 23480))
server_socket.listen(4)

threads = []
while True:
	sock, ip_address = server_socket.accept()
	threads.append(NewClientThread(sock, ip_address, threads))
	threads[-1].start()

server_socket.close()