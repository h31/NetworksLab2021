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
			self.sock.sendall(data)
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