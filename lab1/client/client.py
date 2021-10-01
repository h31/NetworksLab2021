import ast
import socket

import colorama
from colorama import Fore, Back

import message_data


class ServerClosed(Exception):
    pass


class Client:

    def __init__(self):
        try:
            colorama.init()
            self.writing = False
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect(('localhost', 6666))
            self.username = ""
            self.set_username(Fore.GREEN + "Username:")
            self.client_socket.send(
                f"{{'parcelType':'greeting', 'message':'', 'username':'{self.username}', 'attachmentType':'', "
                f"'attachmentName':'', 'attachmentSize':''}}\r\n".encode('utf-8'))

        except TimeoutError:
            print(Back.RED + "Сервер не отвечает")
            self.client_socket.close()

    def set_username(self, reason=''):
        self.username = input(reason).replace("\\", "\\\\").replace("'", "\\\\'")

    def receive(self):
        while True:
            try:
                message = self.client_socket.recv(1024).decode('utf-8')
                # while self.writing:
                #     pass
                try:
                    message = ast.literal_eval(message)
                    if message['parcelType'] == 'exit':
                        raise ServerClosed
                    elif message['parcelType'] == 'greeting':
                        print(f"\n[{message['time']}] {message['username']} вошел в чат")
                    elif message['parcelType'] == 'message':
                        print(f"\n[{message['time']}] {message['username']} сказал: {message['message']}")
                        if 'file' in message.keys():
                            filename = message_data.save_file(message['username'],
                                                              message['attachmentType'], message['attachmentName'],
                                                              message['file'])
                            print(f"Received {filename} from {message['username']}")
                    elif message['parcelType'] == 'exception':
                        if message['message'] == '0':  # TODO
                            pass
                except SyntaxError:
                    fp = message_data.save_file('server', message, '.jpg')
                    print(Fore.YELLOW + f"Медиафайл сохранен в {fp}")
            except ServerClosed:
                print(Back.GREEN + "Всем пока!")
                self.client_socket.close()
                break
            except Exception:
                print()
                print(Back.RED + "Сервер покинул чат")
                self.client_socket.close()
                break

    def write(self):  # TODO: блокировать receive, пока не отработает write
        while True:
            if self.username != '':
                try:
                    self.writing = True
                    message = input().replace("\\", "\\\\").replace("'", "\\\\'")
                    attached = False
                    while not attached:
                        fp = input("Relative filepath:")
                        if fp == '':
                            self.client_socket.send(
                                f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
                                f"'attachmentType':'', "
                                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                            self.writing = False
                            break
                        try:
                            ext, name, size, file = message_data.load_file(fp)
                            self.client_socket.send(
                                f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
                                f"'attachmentType':'{ext}', "
                                f"'attachmentName':'{name}', 'attachmentSize':'{size}'}}\r\n".encode('utf-8'))
                            self.client_socket.send(bytes(file))
                            self.writing = False
                            attached = True
                        except FileNotFoundError:
                            print(Fore.RED + f"File {fp} not found")
                except Exception:
                    print(Back.RED + "Сервер недоступен")
                    break
