import Serialization
from datetime import datetime, timezone
from SocketConnection import SocketConnection

class ClientHandler():
	def __init__(self, sock, ip_address, handlers):
		self.sock = SocketConnection(sock)
		self.ip_address = ip_address
		self.handlers = handlers
		self.nickname = None

	async def registration(self):
		data = await self.sock.async_recv()
		if len(data) == 0: return False
		result = True
		dictionary = Serialization.load(data)
		if not "nickname" in dictionary:
			answer = {"status":"error: nickname is required"}
			result = False
		else:
			new_nickname = Serialization.bytesToStr(dictionary["nickname"])
			if new_nickname == "SERVER": # The nickname "SERVER" is reserved by the server
				unique = False
			else:
				unique = True
				for handler in self.handlers:
					if handler.nickname == new_nickname:
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
	
	async def run(self):
		if not await self.registration():
			self.sock.close()
			return
		
		# notify everyone about the connection of a new user
		message = {"time":self.getCurrentTimeInUTC(), "nickname":"SERVER",
				"text":"User \"" + self.nickname + "\" connected"}
		self.sendToEveryone(message)
		
		while True:
			data = await self.sock.async_recv()
			if len(data) == 0: # socket is closed
				message = {"time":self.getCurrentTimeInUTC(), "nickname":"SERVER",
						"text":"User \"" + self.nickname + "\" disconnected"}
				self.sendToEveryone(message)
				
				self.nickname = None
				self.sock.close()
				break
			dictionary = Serialization.load(data)
			if not ("text" in dictionary): continue # error	
			
			# sending a message to all interlocutors
			message = {"time":self.getCurrentTimeInUTC(), 
					"nickname":self.nickname, "text":dictionary["text"]}
			
			file_data = None
			if "attachment" in dictionary:
				message["attachment"] = dictionary["attachment"]
				file_data = await self.sock.async_recv()
			
			self.sendToEveryone(message)
			if file_data != None:
				self.sendToEveryone(file_data, serialize = False)
	
	def getCurrentTimeInUTC(self):
		return str(datetime.now(tz = timezone.utc))
	
	def sendToEveryone(self, msg, serialize = True):
		for handler in self.handlers:
			if handler.ip_address != self.ip_address:
				if serialize:
					handler.send(Serialization.dump(msg))
				else:
					handler.send(msg)
	
	def send(self, data):
		self.sock.send(data)