import asyncio
import socket


class ChatSocket:

    def __init__(self, sock=None):
        if sock is None:
            self.sock = socket.socket(
                socket.AF_INET, socket.SOCK_STREAM)
        else:
            self.sock = sock

    async def chat_send(self, msg):
        loop = asyncio.get_event_loop()
        await loop.sock_sendall(self.sock, msg)

    async def chat_receive(self):
        loop = asyncio.get_event_loop()
        chunks = []
        bytes_recd = 0
        len_recd = 10
        while bytes_recd < len_recd:
            chunk = await loop.sock_recv(self.sock, (min(len_recd - bytes_recd, 2048)))
            if chunk == b'':
                raise ConnectionError("socket connection broken")
            if bytes_recd == 0:
                len_str = ''
                for byte in chunk:
                    len_str += chr(byte)
                try:
                    len_recd = int(len_str) + len(len_str)
                except ValueError:
                    raise ConnectionError("package structure broken")
            chunks.append(chunk)
            bytes_recd = bytes_recd + len(chunk)
        return b''.join(chunks)
