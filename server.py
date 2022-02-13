import socket
import threading
import time

host = '127.0.0.1'
port = 9090

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind((host, port))
server.listen()

clients = []
nicknames = []

def broadcast(message):   
    for client in clients:
        client.send(message)
   
def handle(client):
    while True:
        try:
            time_now = time.strftime('%H:%M:%S', time.localtime())
            message = client.recv(16384).decode('utf-8')
            new_message = ("[{}]".format(time_now) + message).encode('utf-8') 
            broadcast(new_message)           
        except:
            index = clients.index(client)
            clients.remove(client)
            client.close()
            nickname = nicknames[index]
            print('{} left the chat!'.format(nickname))
            broadcast('{} left the chat!'.format(nickname).encode('utf-8'))
            nicknames.remove(nickname)
            break

def receive():
    while True:
        client, address = server.accept()
        print("Connected with {}".format(str(address)))

        client.send('NAME'.encode('utf-8'))
        nickname = client.recv(1024).decode('utf-8')
        nicknames.append(nickname)
        clients.append(client)
         
        print("Name is {}".format(nickname))
        broadcast("{} joined!".format(nickname).encode('utf-8'))
        client.send('Connected to server!'.encode('utf-8'))

        thread = threading.Thread(target=handle, args=(client,))
        thread.start()

print('Server started')
receive()