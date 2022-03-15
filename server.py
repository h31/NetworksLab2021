import socket
import time

host = socket.gethostbyname(socket.gethostname())
port = 9090

clients = []

server = socket.socket(socket.AF_INET,socket.SOCK_DGRAM)
server.bind((host,port))
print("Server started")

quit = False   
while not quit:
        try:
            data, addr = server.recvfrom(16384)
            if addr not in clients:
                clients.append(addr)
                print("Connected with {}".format(str(addr)))
                
            time_now = time.strftime('%H:%M:%S', time.localtime())

            new_message = ("[{}]".format(time_now) + data.decode('utf-8')).encode('utf-8')
            
            for client in clients:
                server.sendto(new_message, client)

            if "left the chat!" in data.decode('utf-8'):
                print("{} left the chat!".format(str(addr)))
                clients.remove(addr)         
        except:
            print("Server shutdown!")
            for client in clients:
                server.sendto("Server shutdown!".encode('utf-8'), client)
            quit = True

server.close()