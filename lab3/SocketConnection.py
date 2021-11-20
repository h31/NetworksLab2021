import asyncio, socket, struct

class SocketConnection:
	def __init__(self, sock):
		self.sock = sock
	
	def close(self):
		self.sock.shutdown(socket.SHUT_RDWR)
		self.sock.close()
	
	def send(self, data):
		length = struct.pack(">I", len(data)) # > = big endian, I = 4 bytes
		try:
			self.sock.sendall(length)
			self.sock.sendall(data)
		except socket.error:
			return False # error
		return True # success
	
	async def async_send(self, data):
		loop = asyncio.get_event_loop()
		length = struct.pack(">I", len(data)) # > = big endian, I = 4 bytes
		try:
			await loop.sock_sendall(self.sock, length)
			await loop.sock_sendall(self.sock, data)
		except socket.error:
			return False # error
		return True # success
	
	def recv(self):
		try:
			data = self.sock.recv(4)
			if len(data) != 4: return b''
			(length, ) = struct.unpack(">I", data) # > = big endian, I = 4 bytes
			result = b''
			while length != 0:
				data = self.sock.recv(length)
				result += data
				length -= len(data)
		except socket.error:
			result = b''
		return result
	
	async def async_recv(self):
		loop = asyncio.get_event_loop()
		try:
			data = await loop.sock_recv(self.sock, 4)
			if len(data) != 4: return b''
			(length, ) = struct.unpack(">I", data) # > = big endian, I = 4 bytes
			result = b''
			while length != 0:
				data = await loop.sock_recv(self.sock, length)
				result += data
				length -= len(data)
		except socket.error:
			result = b''
		return result