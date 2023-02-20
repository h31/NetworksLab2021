import socket
import threading
from datetime import datetime, timedelta, timezone


FORMAT = 'UTF-8'

server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server.bind(('localhost', 10000))
users = []
userNames = []
server.listen()


def handle_client(user):
    while True:
        try:
            message = user.recv(16384).decode(FORMAT)
            tz_message = message.split(';')
            time_zone = tz_message[1]
            message = message.rsplit(';', 1)[0]
            time_zone_hours = time_zone[0]+time_zone[1]+time_zone[2]
            time_zone_min = time_zone[3]+time_zone[4]
            current_time = (datetime.now(timezone.utc)
                            + timedelta(hours=int(time_zone_hours), minutes=int(time_zone_min)))\
                .strftime('%H:%M:%S')
            new_message = ("[{}".format(current_time) + " {}]".format(time_zone) + message).encode(FORMAT)
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


print('Server started:' + datetime.now(timezone.utc).strftime('%H:%M:%S %Z'))
receive()
