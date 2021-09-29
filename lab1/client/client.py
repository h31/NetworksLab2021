import ast
import socket
import traceback

import message_data


class ServerClosed(Exception):
    pass


class Client:

    def __init__(self):
        try:
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect(('localhost', 6666))
            self.username = ""
            self.set_username("Username:")
            self.client_socket.send(
                f"{{'parcelType':'greeting', 'message':'', 'username':'{self.username}', 'attachment':''}}\r\n".encode(
                    'utf-8'))

        except TimeoutError:
            print("Сервер не отвечал некоторое время")
            self.client_socket.close()

    def set_username(self, reason=''):
        self.username = input(reason)

    def receive(self):
        while True:
            try:
                message = ast.literal_eval(self.client_socket.recv(1024).decode('utf-8'))
                # if message['parcelType'] == 'greetingError':
                #     self.set_nickname()
                #     self.client_socket.send(
                #         f"{{'parcelType':'greeting', 'message':'{self.nickname}'}}\r\n".encode('utf-8'))
                if message['parcelType'] in ['info', 'message']:
                    print(f"\n[{message['time']}] {message['username']} {message['message']}")
                    if 'file' in message.keys():
                        filename = message_data.save_file(message['username'], message['file'],
                                                          message['file_extension'])
                        print(f"Received {filename} from {message['username']}")
                elif message['parcelType'] == 'exception':
                    if message['message'] == '0':  # TODO
                        pass
                elif message['parcelType'] == 'exit':
                    raise ServerClosed

                # {'parcelType':'greeting', 'message':'Добро пожаловать в чат! Введите пожалуйста свой никнейм: ', 'time':'Mon Sep 27 20:26:04 MSK 2021'}


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
            except Exception:
                print()
                print("Сервер невнятно выразился")
                self.client_socket.close()
                break

    def write(self):
        while True:
            if self.username != '':
                try:
                    message = input()
                    message += "\r\n"
                    encoded_message = {}
                    attached = False
                    while not attached:
                        fp = input("Relative filepath:")
                        if fp == '':
                            self.client_socket.send(f"{{'parcelType':'message', 'message':'{message}', "
                                                    f"'username':'{self.username}', 'attachment':''}}\r\n".encode(
                                'utf-8'))

                            # encoded_message = message_data.encode_message("client message without file", self.nickname,
                            #                                               message)
                            break
                        try:
                            ext, file = message_data.load_file(fp)
                            self.client_socket.send(f"{{'parcelType': 'message', 'message': '{message}', "
                                                    f"'username':'{self.username}', 'attachment':'{file}'}}\r\n".encode(
                                'utf-8'))
                            # encoded_message = message_data.encode_message("client message with file", self.nickname,
                            #                                               message, fp)
                            attached = True
                        except FileNotFoundError:
                            print(f"File {fp} not found")
                            pass
                        except:
                            print()
                            print(traceback.format_exc())

                    # self.client_socket.send(encoded_message)
                except Exception:
                    print("Сервер недоступен")
                    print(traceback.format_exc())
                    break
