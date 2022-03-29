import socket
import threading
import time

FORMAT = 'UTF-8'
host = '127.0.0.1'
port = 55555
users = []
user = []
server = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
server.bind((host,port))
print("Server started")

def broadcast(message, addr):
    for user in users:
        if user != addr:
            server.sendto(message, user)

def handle_client():
    while True:
            data, addr = server.recvfrom(65535)
            if addr not in users:
                users.append(addr)
                print("Connected with {}".format(str(addr)))
            else:
                time_now = time.strftime('%H:%M:%S', time.localtime())
                new_message = ("[{}]".format(time_now) + data.decode(FORMAT)).encode(FORMAT)
                broadcast(new_message, user)
                if "left the chat!" in data.decode(FORMAT):
                    print("{} left the chat!".format(str(addr)))
                    users.remove(addr)


thread = threading.Thread(target=handle_client)
thread.start()