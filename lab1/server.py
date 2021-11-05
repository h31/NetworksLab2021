import socket
from clientThread import clientThread

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.bind(('', 12345))
sock.listen(4)

threads = []

while True:
    connection, address = sock.accept()
    threads.append(clientThread(connection, address, threads))
    threads[-1].start()

sock.close()
