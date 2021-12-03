import socket, asyncio

from ForClient import ForClient

sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
sock.bind(('', 9090))
sock.listen(5)

handlers = []


async def run_server():
    loop = asyncio.get_event_loop()
    while True:
        conn, addr = await loop.sock_accept(sock)
        handlers.append(ForClient(conn, addr, handlers))
        loop.create_task(handlers[-1].run())


asyncio.run(run_server())
sock.shutdown(socket.SHUT_RDWR)
sock.close()
