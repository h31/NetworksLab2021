import socket
import time

FORMAT = 'UTF-8'
host = socket.gethostbyname(socket.gethostname())
server = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
server.bind((host,10000))
users = []

exitApp = False
while not exitApp:
        try:
            data, tempUser = server.recvfrom(16384)
            if tempUser not in users:
                users.append(tempUser)
                print("Connected with {}".format(str(tempUser)))
            current_time = time.strftime('%H:%M:%S', time.localtime())
            new_message = ("[{}]".format(current_time) + data.decode(FORMAT)).encode(FORMAT)
            for user in users:
                server.sendto(new_message, user)
            if "left the chat!" in data.decode(FORMAT):
                print("{} left the chat!".format(str(tempUser)))
                users.remove(tempUser)
        except:
            print("Server shutdown!")
            for user in users:
                server.sendto("Server shutdown!".encode(FORMAT), user)
            exitApp = True

print("Server started")
server.close()