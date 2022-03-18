import asyncio
import socket

from request_handler import RequestHandler
from extended_async_socket import ChatSocket

serversocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
serversocket.bind(('localhost', 1024))
serversocket.setblocking(False)
serversocket.listen(5)

names = dict()


async def run_server():
    event_loop = asyncio.get_event_loop()
    while serversocket.fileno() != -1:
        try:
            (clientsocket, address) = await event_loop.sock_accept(serversocket)
            print('Client connected from', address)
            clientsocket = ChatSocket(clientsocket)
            ct = RequestHandler(clientsocket, serversocket, names)
            event_loop.create_task(ct.run())
        except OSError:
            break


asyncio.run(run_server())

print('Server is closed')
serversocket.close()
exit(0)
