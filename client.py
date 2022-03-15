import socket
import threading

host = "192.168.56.1"
port = 9090

client = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)

nickname = input("Please enter your name: ")

def receive():
	while True:
		try:
			data, addr = client.recvfrom(16384)
			print(data.decode('utf-8'))
		except:
			pass
		
def write():
	join = False
	while True:
			if join == False:
				client.sendto("{} joined!".format(nickname).encode("utf-8"), (host, port))
				join = True
			else:
				try:
					message = input()
					if message != "":
						client.sendto("{}:{}".format(nickname, message).encode('utf-8'), (host, port))
				except:
					print("{} left the chat! ".format(nickname))
					client.sendto("{} left the chat! ".format(nickname).encode("utf-8"), (host, port))
					client.close()	
					quit()	

receive_thread = threading.Thread(target=receive)
receive_thread.start()

write_thread = threading.Thread(target=write)
write_thread.start()






















