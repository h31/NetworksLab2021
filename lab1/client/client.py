import socket
import traceback


class ServerClosed(Exception):
    pass


class Client:
    def __init__(self):
        self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.client_socket.connect(('192.168.137.146', 6666))
        self.nickname = ""

    def set_nickname(self, reason):
        self.nickname = input(f"{reason}\nNickname:")

    def receive(self):
        while True:
            try:
                # message = self.client_socket.recv(1024)
                # message = message_data.decode_message(message)
                message = self.client_socket.recv(1024).decode('utf-8')
                # message = json.loads(message)
                print()
                print(message)
                if message == "Доброе пожаловать в чат. Введите пожалуйста свой никнейм:":
                    self.set_nickname(message)
                    response = self.nickname + "\r\n"
                    self.client_socket.send(message.encode('utf-8'))

            # if message["type"] == 'nickname request':
            #     encode_message
            #     self.client_socket.send(self.nickname.encode('ascii'))
            #     self.client_socket.send(encode_message('nickname response', self.nickname, self.nickname.encode('utf-8'), '')
            # if message['type'] == 'nickname taken':
            #     self.set_nickname("Nickname already used.")
            #     self.client_socket.send(encode_message('nickname response', self.nickname, self.nickname.encode('utf-8'), '')
            # if message['type'] == 'invalid nickname':
            #     self.set_nickname("Invalid nickname.")
            #     self.client_socket.send(encode_message('nickname response', self.nickname, self.nickname.encode('utf-8'), '')
            # elif message['type'] == 'server closed':
            #     raise ServerClosed
            # else:
            #     print(f"{message[1]} <{message[2]}>: {message[3]} (file {message[4]} attached)")
            except ServerClosed:
                print("Server closed")
                self.client_socket.close()
                break
            except:
                print()
                print(traceback.format_exc())
                print("An error occurred")
                self.client_socket.close()
                break

    def write(self):
        while True:
            try:
                message = input(f'{self.nickname}: ')
                message += "\r\n"
                # attached = False
                # encmes = {}
                # while not attached:
                #     fp = input("Relative filepath:")
                #     try:
                #         encmes = message_data.encode_message("Client message", self.nickname, message, fp)
                #         attached = True
                #     except FileNotFoundException:
                #         print(f"File not found in {fp}")
                #         pass
                #     except:
                #         print()
                #         print(traceback.format_exc())

                # self.client_socket.send(encmes)
                self.client_socket.send(message.encode('utf-8'))
            except Exception:
                print("Not today")
                break
