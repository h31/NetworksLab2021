import socket

from ThreadForClient import ThreadForClient

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.bind(('', 9090))
sock.listen(5)

threads = []
while True:
    conn, addr = sock.accept()
    threads.append(ThreadForClient(conn, addr, threads))
    threads[-1].start()

sock.close()

