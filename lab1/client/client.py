import socket
import time

import colorama
from colorama import Fore, Back, Style

import message_data


class Client:

    def __init__(self):
        colorama.init()
        try:
            self.logged = False
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect(('localhost', 6666))
            self.username = ""
            self.set_username(Back.BLACK + Fore.GREEN + "Username:")
            self.client_socket.send(
                f"{{'parcelType':'greeting', 'message':'', 'username':'{self.username}', "
                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))

        except TimeoutError:
            print(Fore.RED + Back.BLACK + "Сервер не отвечает" + Style.RESET_ALL)
            self.client_socket.close()

    def set_username(self, reason=''):
        self.username = input(reason).replace("\\", "\\\\").replace("'", "\\'")

    def receive(self):
        '''Метод получения данных от сервера('''
        while True:
            try:
                message = bytes()
                letter = self.client_socket.recv(1)
                while letter != b'\r':
                    message += letter
                    letter = self.client_socket.recv(1)
                self.client_socket.recv(1)
                message = message.decode('utf-8', 'ignore')
                message = message_data.parse_message(message)
                if message['parcelType'] == 'exit':
                    print(
                        Fore.YELLOW + Back.BLACK + f"\n[{message_data.time_format(message['time'])}] "
                                                   f"{message['username']} покинул чат" + Style.RESET_ALL)
                elif message['parcelType'] == 'greeting':
                    print(
                        Fore.GREEN + Back.BLACK + f"\n[{message_data.time_format(message['time'])}] "
                                                  f"{message['username']} вошел в чат" + Style.RESET_ALL)
                    if message['username'] == self.username:
                        self.logged = True
                        print(Back.BLACK + Fore.GREEN + "Если тебе надоест общение, напиши !exit\n" + Style.RESET_ALL)
                elif message['parcelType'] == 'message':
                    print(
                        Fore.GREEN + Back.BLACK + f"\n[{message_data.time_format(message['time'])}] "
                                                  f"{message['username']} сказал: {message['message']}"
                        + Style.RESET_ALL)
                    if message['attachmentSize'] != '0' and message['username'] != self.username:
                        size = int(message['attachmentSize'])
                        count = size
                        attachment = b''
                        while count > 0:
                            attachment += bytes(self.client_socket.recv(count))
                            count = size - len(attachment)
                        # attachment = b''
                        # for _ in range(int(message['attachmentSize'])):
                        #     attachment += self.client_socket.recv(1)
                        message_data.save_file(message['username'], message['attachmentName'], attachment)
                        print(
                            Fore.BLUE + Back.YELLOW + f"Received {message['attachmentName']} "
                                                      f"from {message['username']}" + Style.RESET_ALL)
                elif message['parcelType'] == 'exception':
                    if message['message'] == '1':
                        print(Fore.RED + Back.BLACK + 'Имя пользователя уже занято!\n' + Style.RESET_ALL)
                        self.set_username(Back.BLACK + Fore.GREEN + "Username:")
                        self.client_socket.send(
                            f"{{'parcelType':'greeting', 'message':'', 'username':'{self.username}', "
                            f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                    else:
                        print(Fore.RED + Back.BLACK + 'Неизвестная ошибка сервера' + Style.RESET_ALL)
            except ConnectionAbortedError:
                break
            except Exception:
                self.client_socket.close()
                break

    def write(self):
        """Метод отправки данных от клиента"""
        while True:
            if self.username != '' and self.logged:
                try:
                    message = input(Fore.GREEN + Back.BLACK + "Message\n").replace("\\", "\\\\").replace("'", "\\'")
                    if message == "!exit":
                        self.client_socket.send(
                            f"{{'parcelType':'exit', 'message':'', 'username':'{self.username}', 'attachmentName':'', "
                            f"'attachmentSize':'0'}}\r\n".encode('utf-8'))
                        self.client_socket.close()
                        self.logged = False
                        break
                    attached = False
                    while not attached:
                        fp = input(Fore.GREEN + Back.BLACK + "Relative filepath\n")
                        if fp == "!exit":
                            self.client_socket.send(
                                f"{{'parcelType':'exit', 'message':'', 'username':'{self.username}', "
                                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                            self.client_socket.close()
                            self.logged = False
                            break
                        if fp == '':
                            self.client_socket.send(
                                f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
                                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                            break
                        try:
                            name, size, file = message_data.load_file(fp)
                            self.client_socket.send(
                                f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
                                f"'attachmentName':'{name}', 'attachmentSize':'{size}'}}\r\n".encode('utf-8'))
                            # for i in file:
                            #     self.client_socket.send(i)
                            time.sleep(1)
                            self.client_socket.sendall(bytes(file))
                            print(Fore.GREEN + Back.BLACK + "File sent" + Style.RESET_ALL)
                            attached = True
                        except FileNotFoundError:
                            print(Fore.BLUE + Fore.RED + f"File {fp} not found" + Style.RESET_ALL)

                except Exception:
                    print(Fore.YELLOW + Back.RED + "Сервер недоступен" + Style.RESET_ALL)
                    break
