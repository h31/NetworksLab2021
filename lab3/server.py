import socket, asyncio

from clientThread import ClientThread

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.bind(('', 12345))
sock.setblocking(False)
sock.listen(4)

threads = []


async def server_work():
    event_loop = asyncio.get_event_loop()
    while True:
        connection, address = await event_loop.sock_accept(sock)
        threads.append(ClientThread(connection, address, threads))
        event_loop.create_task(threads[-1].run())


asyncio.run(server_work())

sock.shutdown(socket.SHUT_RDWR)
sock.close()
