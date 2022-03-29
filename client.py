import socket
import threading
import sys

FORMAT = 'UTF-8'
name = input("Please enter your name: ")
user = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)


def message_receive():
	while True:
		try:
			data, addr = user.recvfrom(16384)
			print(data.decode(FORMAT))
		except:
			pass
		
def message_send():
	join = False
	while True:
			if not join:
				user.sendto("{} joined!".format(name).encode(FORMAT), ("192.168.56.1", 10000))
				join = True
			else:
				try:
					message = input()
					if message != "":
						user.sendto("{}:{}".format(name, message).encode(FORMAT), ("192.168.56.1", 10000))
				except:
					print("{} left the chat! ".format(name))
					user.sendto("{} left the chat! ".format(name).encode(FORMAT), ("192.168.56.1", 10000))
					user.close()
					sys.exit(0)

message_receive_thread = threading.Thread(target=message_receive)
message_receive_thread.start()
message_send_thread = threading.Thread(target=message_send)
message_send_thread.start()
