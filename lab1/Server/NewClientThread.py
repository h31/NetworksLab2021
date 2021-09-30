import threading, Serialization
from datetime import datetime, timezone
from SocketConnection import SocketConnection

class NewClientThread(threading.Thread):
	def __init__(self, sock, ip_address, threads):
		threading.Thread.__init__(self)
		self.sock = SocketConnection(sock)
		self.ip_address = ip_address
		self.threads = threads
		self.nickname = None

	def registration(self):
		result = True
		data = self.sock.recv()
		dictionary = Serialization.load(data)
		if not "nickname" in dictionary:
			answer = {"status":"error: nickname is required"}
			result = False
		else:
			new_nickname = Serialization.bytesToStr(dictionary["nickname"])
			if new_nickname == "SERVER": # The nickname "SERVER" is reserved
				unique = False
			else:
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
				result = False
		self.sock.send(Serialization.dump(answer))
		return result
	
	def run(self):
		if not self.registration():
			self.sock.close()
			return
		
		# notify everyone about the connection of a new user
		message = {"time":self.getCurrentTimeInUTC(), "nickname":"SERVER",
				"text":"User " + self.nickname + " connected"}
		self.sendToEveryone(message)
		
		while True:
			data = self.sock.recv() # receiving new message
			dictionary = Serialization.load(data)
			if dictionary == None: # socket is closed
				message = {"time":self.getCurrentTimeInUTC(), "nickname":"SERVER",
						"text":"User " + self.nickname + " disconnected"}
				self.sendToEveryone(message)
				
				self.nickname = None
				self.sock.close()
				break
			
			# error	
			if not ("text" in dictionary): continue
			
			# sending a message to all interlocutors
			message = {"time":self.getCurrentTimeInUTC(), 
					"nickname":self.nickname, "text":dictionary["text"]}
			
			file_data = None
			if "attachment" in dictionary:
				message["attachment"] = dictionary["attachment"]
				file_data = self.sock.recv()
			
			self.sendToEveryone(message)
			if file_data != None:
				self.sendToEveryone(file_data, serialize = False)
	
	def getCurrentTimeInUTC(self):
		return str(datetime.now(tz = timezone.utc))
	
	def sendToEveryone(self, msg, serialize = True):
		for thread in self.threads:
			if thread.ip_address != self.ip_address:
				if serialize:
					thread.send(Serialization.dump(msg))
				else:
					thread.send(msg)
	
	def send(self, data):
		self.sock.send(data)