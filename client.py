import socket
import threading

FORMAT = 'UTF-8'
host = '127.0.0.1'
port = 55555
user = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
user.connect((host, 55555))
name = input("Please enter your name: ")

def message_receive():
	while True:
			data, addr = user.recvfrom(16384)
			print(data.decode(FORMAT))

def message_send():
	join = False
	while True:
			if not join:
				user.sendto("{} joined!".format(name).encode(FORMAT), (host, port))
				join = True
			try:
				message = input()
				if message != "":
					user.sendto("{}:{}".format(name, message).encode(FORMAT), (host, port))
			except:
				print("{} left the chat! ".format(name))
				user.sendto("{} left the chat! ".format(name).encode(FORMAT), (host, port))
				user.close()
				quit()

receive_thread = threading.Thread(target=message_receive)
receive_thread.start()
write_thread = threading.Thread(target=message_send)
write_thread.start()
