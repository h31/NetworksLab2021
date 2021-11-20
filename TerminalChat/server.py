from extended_socket import ChatSocket
import client_thread
import threading
import socket

names = dict()


if __name__ == '__main__':
    serversocket = None
    try:
        serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    except OSError:
        exit(-1)
    try:
        serversocket.bind(('localhost', 1024))
        semaphore = threading.BoundedSemaphore(1)
        serversocket.listen(5)
    except OSError:
        serversocket.close()
        exit(-1)
    while serversocket.fileno() != -1:
        try:
            (clientsocket, address) = serversocket.accept()
        except OSError:
            print('Server is closed')
            serversocket.close()
            exit(0)
        print('Client connected from', address)
        clientsocket = ChatSocket(clientsocket)
        ct = client_thread.ClientThread(clientsocket, semaphore, serversocket)
        ct.start()



