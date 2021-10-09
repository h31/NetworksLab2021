import socket, sys, threading, Serialization
from pathlib import Path
from datetime import datetime
from SocketConnection import SocketConnection

# launch example: python Client.py Ivan_Ivanov
if len(sys.argv) != 2:
	print("Missed nickname")
	exit()

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
try:
	client_socket.connect(("networkslab-ivt.ftp.sh", 23480)) # networkslab-ivt.ftp.sh / 127.0.0.1
except socket.error:
	print("Server is unavailable")
	exit()
sock = SocketConnection(client_socket)

# registration on the server
message = {"nickname":sys.argv[1]}
sock.send(Serialization.dump(message))
dictionary = Serialization.load(sock.recv())
if "status" in dictionary:
	if Serialization.bytesToStr(dictionary["status"]) != "success":
		print("Connection failed: choose a different nickname")
		exit()
else:
	exit()

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

def getMessage(disconnect_event):
	while True:
		data = sock.recv()
		if len(data) == 0: # socket is closed
			print("\nDisconnected")
			disconnect_event.set()
			break
		
		dictionary = Serialization.load(data)
		if "time" in dictionary and "nickname" in dictionary and "text" in dictionary:
			time = getLocalTime(Serialization.bytesToStr(dictionary["time"]))
			print("<" + time + "> [" 
				+ Serialization.bytesToStr(dictionary["nickname"]) + "] " 
				+ Serialization.bytesToStr(dictionary["text"]), end = "")
		
		if "attachment" in dictionary:
			file_data = sock.recv() # receive attachment (file)
			file_name = getNonExistentName(Serialization.bytesToStr(dictionary["attachment"]))
			with open(file_name, "wb") as f:
				f.write(file_data)
			print(" (" + file_name + " attached)", end = "")
		print()

disconnect_event = threading.Event()
listenThread = threading.Thread(target = getMessage, args=(disconnect_event,))
listenThread.start()

print("Enter \q to exit")
while True:
	text = input("NEW MESSAGE:\n")
	if disconnect_event.is_set(): break
	if text == "\q": break
	
	message = {}
	message["text"] = text
	
	file_name = input("ATTACHMENT (FILE NAME OR NOTHING):\n")
	if disconnect_event.is_set(): break
	if file_name == "\q": break
	
	file_data = None
	if len(file_name) != 0:
		if (Path.cwd() / file_name).exists():
			with open(file_name, "rb") as f:
				file_data = f.read()
			if len(file_data) != 0:
				message["attachment"] = file_name
			else:
				print("Empty file")
		else:
			print("File not found")
	
	if not sock.send(Serialization.dump(message)): # sending failed
		print("\nDisconnected")
		break
	
	if "attachment" in message:
		if not sock.send(file_data): # sending failed
			print("\nDisconnected")
			break

sock.close()
listenThread.join()