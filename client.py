import socket
import threading
import time

nickname = input("Please enter your name: ")

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(('127.0.0.1', 9090))

time_now = time.strftime('%H:%M:%S', time.localtime())

def receive():
    while True:
        try:
            message = client.recv(1024).decode('utf-8')
            if message == 'NAME':
                client.send(nickname.encode('utf-8'))
            else:
                print(message)           
        except:
            print("Server shutdown!")
            client.close()
            break

def write():
    while True:
        try:
            mes = input("")
            message = '[{}]{}: {}'.format(time_now,nickname, mes)
            client.send(message.encode('utf-8'))
        except:
            print("Server shutdown!")
            client.close()
            break    

receive_thread = threading.Thread(target=receive)
receive_thread.start()

write_thread = threading.Thread(target=write)
write_thread.start()