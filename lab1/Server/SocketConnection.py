import socket, struct

class SocketConnection:
	def __init__(self, sock):
		self.sock = sock
	
	def close(self):
		self.sock.close()
	
	def send(self, data):
		length = struct.pack(">I", len(data)) # > = big endian, I = 4 bytes
		try:				           
			self.sock.sendall(length)
			self.sock.send(data)
		except socket.error:
			return False # error
		return True # success
		
	def recv(self):
		try:
			(length, ) = struct.unpack(">I", self.sock.recv(4))
			data = b''
			while len(data) < length:
				data += self.sock.recv(2048)
		except socket.error:
			data = b''
		return data