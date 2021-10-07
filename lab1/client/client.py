import ast
import socket
import traceback

import colorama
from colorama import Fore, Back

import message_data


class Client:

    def __init__(self):
        try:
            colorama.init()
            self.logged = False
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect(('localhost', 6666))
            self.username = ""
            self.set_username(Back.BLACK + Fore.GREEN + "Username:")
            self.client_socket.send(
                f"{{'parcelType':'greeting', 'message':'', 'username':'{self.username}', 'attachmentType':'', "
                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))

        except TimeoutError:
            print(Fore.RED + Back.BLACK + "Сервер не отвечает")
            self.client_socket.close()

    def set_username(self, reason=''):
        self.username = input(reason).replace("\\", "\\\\").replace("'", "\\'")

    def receive(self):
        while True:
            try:
                message = bytes()
                letter = self.client_socket.recv(1)
                while letter != b'\n':
                    message += letter
                    letter = self.client_socket.recv(1)
                message = message.decode('utf-8')
                message = ast.literal_eval(message)
                if message['parcelType'] == 'exit':
                    print(
                        Fore.YELLOW + Back.BLACK + f"\n[{message_data.time_format(message['time'])}] {message['username']} покинул чат")
                elif message['parcelType'] == 'greeting':
                    print(
                        Fore.GREEN + Back.BLACK + f"\n[{message_data.time_format(message['time'])}] {message['username']} вошел в чат")
                    if message['username'] == self.username:
                        self.logged = True
                        print(Back.BLACK + Fore.GREEN + "Если тебе надоест общение, напиши !exit\n")
                elif message['parcelType'] == 'message':
                    print(
                        Fore.GREEN + Back.BLACK + f"\n[{message_data.time_format(message['time'])}] {message['username']} сказал: {message['message']}")
                    if message['attachmentSize'] != '0':
                        attachment = b''
                        self.client_socket.recv(1024)
                        print("Получил файл от сервера")
                        message_data.save_file(message['username'], message['attachmentType'],
                                               message['attachmentName'], attachment)
                        print(
                            Fore.BLUE + Back.YELLOW + f"Received {message['attachmentName']} from {message['username']}")
                elif message['parcelType'] == 'exception':
                    if message['message'] == '1':  # TODO
                        print(Fore.RED + Back.BLACK + 'Имя пользователя уже занято!\n')
                        self.set_username(Back.BLACK + Fore.GREEN + "Username:")
                        self.client_socket.send(
                            f"{{'parcelType':'greeting', 'message':'', 'username':'{self.username}', 'attachmentType':'', "
                            f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                    else:
                        print(Fore.RED + Back.BLACK + 'Неизвестная ошибка сервера')
            except ConnectionAbortedError:
                break
            except Exception:
                print(Fore.RED + Back.BLACK + '\n' + traceback.format_exc())
                self.client_socket.close()
                break

    def write(self):
        while True:
            if self.username != '' and self.logged:
                try:
                    message = input(Fore.GREEN + Back.BLACK + "Message\n").replace("\\", "\\\\").replace("'", "\\'")
                    if message == "!exit":
                        self.client_socket.send(
                            f"{{'parcelType':'exit', 'message':'', 'username':'{self.username}', 'attachmentType':'', "
                            f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                        self.client_socket.close()
                        self.logged = False
                        break
                    attached = False
                    while not attached:
                        fp = input(Fore.GREEN + Back.BLACK + "Relative filepath\n")
                        if message == "!exit":
                            self.client_socket.send(
                                f"{{'parcelType':'exit', 'message':'', 'username':'{self.username}', 'attachmentType':'', "
                                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                            self.client_socket.close()
                            self.logged = False
                            break
                        if fp == '':
                            self.client_socket.send(
                                f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
                                f"'attachmentType':'', "
                                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                            break
                        try:
                            name, ext, size, file = message_data.load_file(fp)
                            self.client_socket.send(
                                f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
                                f"'attachmentType':'{ext}', "
                                f"'attachmentName':'{name}', 'attachmentSize':'{size}'}}\r\n".encode('utf-8'))
                            # for i in file:
                            #     self.client_socket.send(i)
                            self.client_socket.send(bytes(file))
                            print("done sending")
                            attached = True
                        except FileNotFoundError:
                            print(Fore.BLUE + Fore.RED + f"File {fp} not found")
                except Exception:
                    print(traceback.format_exc())
                    print(Fore.YELLOW + Back.RED + "Сервер недоступен")
                    break