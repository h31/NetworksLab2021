import socket


class ServerClosed(Exception):
    pass


keep_running = False


class Client:

    def __init__(self):
        try:
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect(('192.168.137.146', 6666))
            self.nickname = ""
            global keep_running
            keep_running = True
        except TimeoutError:
            print("В настоящее время сервер недоступен")

    def set_nickname(self, reason):
        self.nickname = input(reason)

    def receive(self):
        global keep_running
        while keep_running:
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
            #     self.set_nickname("Данное имя пользователя уже используется. Введите другое имя пользователя:")
            #     self.client_socket.send(encode_message('nickname response', self.nickname, self.nickname.encode('utf-8'), '')
            # if message['type'] == 'invalid nickname':
            #     self.set_nickname("Тут так не принято. Введите другое имя пользователя:")
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
                print("В настоящее время сервер недоступен")
                keep_running = False
                self.client_socket.close()
                break

    def write(self):
        global keep_running
        while keep_running:
            try:
                message = input(f'{self.nickname}:')
                message += "\r\n"
                # attached = False
                # encmes = {}
                # while not attached:
                #     fp = input("Relative filepath:")
                #     try:
                #         encoded_message = message_data.encode_message("Client message", self.nickname, message, fp)
                #         attached = True
                #     except FileNotFoundException:
                #         print(f"File not found in {fp}")
                #         pass
                #     except:
                #         print()
                #         print(traceback.format_exc())

                # self.client_socket.send(encoded_message)
                self.client_socket.send(message.encode('utf-8'))
            except Exception:
                keep_running = False
                print("Сервер недоступен")
                break
