import threading, Serialization
from datetime import datetime
from SocketConnection import SocketConnection

class NewClientThread(threading.Thread):
	def __init__(self, sock, ip_address, threads):
		threading.Thread.__init__(self)
		self.sock = SocketConnection(sock)
		self.ip_address = ip_address
		self.threads = threads
		self.nickname = None
	
	def send(self, data):
		self.sock.send(data)

	def registration(self):
		status = True
		data = self.sock.recv()
		dictionary = Serialization.load(data)
		if not b'nickname' in dictionary:
			answer = {"status":"error: nickname required"}
			status = False
		else:
			new_nickname = Serialization.bytesToStr(dictionary[b'nickname'])
			unique = True
			for thread in self.threads:
				if thread.nickname == new_nickname:
					unique = False
					break
			if unique:
				self.nickname = new_nickname
				answer = {"status":"success"}
			else:
				answer = {"status":"error: this nickname is already taken"}
				status = False
		self.sock.send(Serialization.dump(answer))
		return status
	
	def run(self):
		if not self.registration():
			self.sock.close()
		else:
			print("User " + self.nickname + " connected")
			while True:
				data = self.sock.recv()
				dictionary = Serialization.load(data)
				if dictionary == None:
					print("User " + self.nickname + " disconnected")
					self.nickname = None
					self.sock.close()
					break
					
				if not (b'text' in dictionary): #error
					continue
				else: # sending a message to all interlocutors
					message = {}
					message["time"] = datetime.now().strftime('%H:%M')
					message["nickname"] = self.nickname
					message["text"] = dictionary[b'text']
					
					if b'attachment' in dictionary and b'data' in dictionary:
						message["attachment"] = dictionary[b'attachment']
						message["data"] = dictionary[b'data']
					
					for thread in self.threads:
						if thread.ip_address != self.ip_address:
							thread.send(Serialization.dump(message))