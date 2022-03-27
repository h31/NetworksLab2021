import socket
from threading import Thread

PORT_TEMPLATE = 8000
SERVER_TEMPLATE = "localhost"
HEADER = 64
FORMAT = 'UTF-8'
DISCONNECT_MESSAGE = "!q"
dict = {}


class Server:

    def __init__(self):
        self.server = None

    def server_conditions_check(self):
        address_value = input("Waiting for IP address. (Leave blanc if server going to start on local machine) ")
        port_value = input("Waiting for port. (Leave blanc for template) ")
        if not address_value:
            address = SERVER_TEMPLATE
        else:
            address = address_value
        if not port_value:
            port = PORT_TEMPLATE
        else:
            port = port_value
        data = (address, port)
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.server.bind(data)

    def server_start(self):
        self.server.listen()
        while True:
            try:
                token, addr = self.server.accept()
                print(f"[NEW CONNECTION] {addr} connected.")
                sock = Thread(target=self.handle_client, args=(token,))
                sock.start()
            except:
                exception_message = "Something gone wrong"
                self.send_to_all(exception_message.encode(FORMAT))
                for c in dict.keys():
                    c.close()
                dict.clear()
                self.server.close()

    def handle_client(self, token):
        connected = True
        while connected:
            try:
                if token not in dict.keys():
                    message_length = int(token.recv(HEADER).decode(FORMAT))
                    name = token.recv(message_length).decode(FORMAT)
                    dict[token] = name
                    message = "To quit from chat type !DISCONNECT"
                    message = f"{len(message):<{HEADER}}" + message
                    token.send(message.encode(FORMAT))
                else:
                    message_length = int(token.recv(HEADER).decode(FORMAT))
                    message = token.recv(message_length)
                    diff = message_length - len(message)
                    while diff != 0:
                        message += token.recv(message_length)
                        diff = message_length - len(message)
                    message = [m.decode(FORMAT) for m in message.split(b'\0')]
                    if message[2] != DISCONNECT_MESSAGE:
                        time = message[0].encode(FORMAT)
                        name = message[1].encode(FORMAT)
                        text = message[2].encode(FORMAT)
                        message = b'\0'.join([time, name, text])
                        self.send_to_all(message)
                    else:
                        self.client_disconnect(token)
                        connected = False
            except:
                print('You have been disconnected')
                self.send_to_all(DISCONNECT_MESSAGE)
                break

    @staticmethod
    def send_to_all(message):
        message = f'{len(message):<{HEADER}}'.encode('utf-8') + message
        for client in dict.keys():
            client.send(message)

    @staticmethod
    def client_disconnect(client):
        message = "You are disconnected from the server"
        message_header = f"{len(message):<{HEADER}}"
        exit_msg = message_header + message
        client.send(exit_msg.encode(FORMAT))
        client.close()
        dict.pop(client)


if __name__ == '__main__':
    print("[STARTING] server is starting...")
    work = Server()
    work.server_conditions_check()
    print(f"[LISTENING] Server is started")
    work.server_start()
