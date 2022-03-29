import socket
import threading
import time

FORMAT = 'UTF-8'

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(('localhost', 10000))
users = []
userNames = []
server.listen()



def handle_client(user):
    while True:
        try:
            current_time = time.strftime('%H:%M:%S', time.localtime())
            message = user.recv(16384).decode(FORMAT)
            new_message = ("[{}]".format(current_time) + message).encode(FORMAT)
            broadcast(new_message)
        except:
            index = users.index(user)
            users.remove(user)
            user.close()
            name = userNames[index]
            print('{} left the chat!'.format(name))
            broadcast('{} left the chat!'.format(name).encode(FORMAT))
            userNames.remove(name)
            break


def receive():
    while True:
        user, address = server.accept()
        print("Connected with {}".format(str(address)))
        user.send('NAME'.encode(FORMAT))
        name = user.recv(1024).decode(FORMAT)
        userNames.append(name)
        users.append(user)
        print("New user named: {}".format(name))
        broadcast("{} joined!".format(name).encode(FORMAT))
        user.send('Connected to server!'.encode(FORMAT))
        thread = threading.Thread(target=handle_client, args=(user,))
        thread.start()


def broadcast(message):
    for user in users:
        user.send(message)


print('Server started')
receive()
