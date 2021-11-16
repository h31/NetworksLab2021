import socket

from clientThread import ClientThread

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.bind(('', 12345))
sock.listen(4)

threads = []

while True:
    connection, address = sock.accept()
    threads.append(ClientThread(connection, address, threads))
    threads[-1].start()
