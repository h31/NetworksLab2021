import socket, sys, threading, Serialization, base64
from pathlib import Path
from datetime import datetime
from SocketConnection import SocketConnection

# launch example: python Client.py Ivan_Ivanov
if len(sys.argv) != 2:
	print("Missed nickname")
	exit(1)

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
try:
	client_socket.connect(("127.0.0.1", 12345))
except socket.error:
	print("Server is unavailable")
	exit(2)
sock = SocketConnection(client_socket)

# registration on the server
message = {"nickname":sys.argv[1]}
sock.send(Serialization.dump(message))
dictionary = Serialization.load(sock.recv())
if "status" in dictionary:
	if Serialization.bytesToStr(dictionary["status"]) == "success":
		print("Connected")
	else:
		print("Connection failed: choose a different nickname")
		exit(3)
else:
	exit(3)

def getNonExistentName(file_name):
	parts = file_name.split(".")
	name = parts[0]
	if len(parts) == 1:
		extension = ""
	else:
		extension = "." + parts[1]
	while True:
		path = Path.cwd() / (name + extension)
		if path.exists():
			name += "_new"
		else:
			break
	return name + extension

def getLocalTime(utcTime):
	return str(datetime.fromisoformat(utcTime).astimezone().strftime('%H:%M'))

def getMessage():
	while True:
		dictionary = Serialization.load(sock.recv())
		if dictionary == None: # socket is closed
			print("\nDisconnected")
			break
		
		if "time" in dictionary and "nickname" in dictionary and "text" in dictionary:
			time = getLocalTime(Serialization.bytesToStr(dictionary["time"]))
			print("<" + time + "> [" 
				+ Serialization.bytesToStr(dictionary["nickname"]) + "] " 
				+ Serialization.bytesToStr(dictionary["text"]), end = "")
		
		if "attachment" in dictionary and "data" in dictionary:
			file_name = getNonExistentName(Serialization.bytesToStr(dictionary["attachment"]))
			with open(file_name, "wb") as f:
				f.write(base64.b64decode(dictionary["data"]))
			print(" (" + file_name + " attached)", end = "")
		print()

listenThread = threading.Thread(target = getMessage)
listenThread.start()

print("Enter \q to exit")
while True:
	text = input()
	if text == "\q": break
	
	message["text"] = text
	
	file_name = input("attachment: ")
	if len(file_name) != 0:
		if (Path.cwd() / file_name).exists():
			message["attachment"] = file_name
			with open(message["attachment"], "rb") as f:
				message["data"] = base64.b64encode(f.read())
		else:
			print("File not found")
	
	if not sock.send(Serialization.dump(message)): # sending failed
		print("\nDisconnected")
		break

sock.close()
listenThread.join()