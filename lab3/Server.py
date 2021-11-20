import asyncio, socket, sys
from ClientHandler import ClientHandler

if len(sys.argv) != 2:
	print("Args: <port number>")
	exit()

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind(("", int(sys.argv[1])))
server_socket.setblocking(False)
server_socket.listen(5)

handlers = []
async def run_server():
	loop = asyncio.get_event_loop()
	while True:
		sock, ip_address = await loop.sock_accept(server_socket)
		handlers.append(ClientHandler(sock, ip_address, handlers))
		loop.create_task(handlers[-1].run())

asyncio.run(run_server())

server_socket.shutdown(socket.SHUT_RDWR)
server_socket.close()