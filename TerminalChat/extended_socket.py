import socket


class ChatSocket:

    def __init__(self, sock=None):
        if sock is None:
            self.sock = socket.socket(
                socket.AF_INET, socket.SOCK_STREAM)
        else:
            self.sock = sock

    def chat_send(self, msg):
        self.sock.sendall(msg)

    def chat_receive(self):
        chunks = []
        bytes_recd = 0
        len_recd = 10
        while bytes_recd < len_recd:
            chunk = self.sock.recv(min(len_recd - bytes_recd, 2048))
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
