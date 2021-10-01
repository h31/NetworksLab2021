import ast
import socket
import time
import traceback

import colorama
from colorama import Fore, Back

import message_data


class ServerClosed(Exception):
    pass


class Client:
    def __init__(self):
        try:
            colorama.init()
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.client_socket.connect(('192.168.152.249', 6666))
            self.username = ""
            self.set_username(Back.WHITE + Fore.GREEN + "Username:")
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
            # if not self.writing:
            try:
                message = bytes()
                letter = self.client_socket.recv(1)
                while letter != b'\n':
                    message += letter
                    letter = self.client_socket.recv(1)
                message = message.decode('latin-1')
                message = ast.literal_eval(message)
                if message['parcelType'] == 'exit':
                    raise ServerClosed
                elif message['parcelType'] == 'greeting':
                    print(Fore.GREEN + Back.BLACK + f"\n[{message['time']}] {message['username']} вошел в чат")
                elif message['parcelType'] == 'message':
                    print(
                        Fore.GREEN + Back.BLACK + f"\n[{message['time']}] {message['username']} сказал: {message['message']}")
                    if message['attachmentSize'] != '0':
                        time.sleep(1)
                        attachment = b''
                        for _ in range(int(message['attachmentSize'])):
                            attachment += self.client_socket.recv(1)
                        message_data.save_file(message['username'], message['attachmentType'],
                                               message['attachmentName'], attachment)
                        print(
                            Fore.BLUE + Back.YELLOW + f"Received {message['attachmentName']} from {message['username']}")
                elif message['parcelType'] == 'exception':
                    if message['message'] == '0':  # TODO
                        pass
            except ServerClosed:
                print(Fore.WHITE + Back.GREEN + "Всем пока!")
                self.client_socket.close()
                break
            except Exception:
                print(Fore.RED + Back.BLACK + '\n' + traceback.format_exc())
                self.client_socket.close()
                break

    def write(self):
        while True:
            if self.username != '':
                try:
                    message = input(Fore.GREEN + Back.BLACK + "Message:").replace("\\", "\\\\").replace("'", "\\'")
                    attached = False
                    while not attached:
                        fp = input(Fore.GREEN + Back.BLACK + "Relative filepath:")
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
                            for i in file:
                                self.client_socket.send(i)
                            attached = True
                        except FileNotFoundError:
                            print(Fore.BLUE + Fore.RED + f"File {fp} not found")
                except Exception:
                    print(traceback.format_exc())
                    print(Fore.YELLOW + Back.RED + "Сервер недоступен")
                    break
