import asyncio
from concurrent.futures.thread import ThreadPoolExecutor

import message_data


class Client:

    def __init__(self):
        self.reader, self.writer = [None, None]
        self.username = ""

    def set_username(self, reason=''):
        self.username = input(reason).replace("\\", "\\\\").replace("'", "\\'")

    async def connect(self):
        try:
            self.reader, self.writer = await asyncio.open_connection('localhost', 6666)
            self.set_username("Username:")
            self.writer.write(
                f"{{'parcelType':'greeting', 'message':'', 'username':'{self.username}', 'attachmentName':'', "
                f"'attachmentSize':'0'}}\r\n".encode('utf-8'))
            await self.writer.drain()
        except TimeoutError:
            print("Сервер не отвечает")
            self.writer.close()
            await self.writer.wait_closed()

    def print_exit(self, message):
        print(f"\n[{message_data.time_format(message['time'])}] {message['username']} покинул чат")

    def print_greeting(self, message):
        print(f"\n[{message_data.time_format(message['time'])}] {message['username']} вошел в чат")
        if message['username'] == self.username:
            print("Если тебе надоест общение, напиши !exit\n")

    async def print_message(self, message):
        print(f"\n[{message_data.time_format(message['time'])}] {message['username']}: {message['message']}")
        if message['attachmentSize'] != '0' and message['username'] != self.username:
            size = int(message['attachmentSize'])
            count = size
            attachment = bytearray(b'')
            while count > 0:
                attachment += await self.reader.read(count)
                count = size - len(attachment)
            message_data.save_file(message['username'], message['attachmentName'], attachment)
            print(f"Received {message['attachmentName']} from {message['username']}")

    async def print_exception(self, message):
        if message['message'] == '1':
            print("Имя пользователя уже занято!\n")
            self.set_username("Username:")
            self.writer.write(
                f"{{'parcelType':'greeting', 'message':'', 'username':'{self.username}', "
                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
            await self.writer.drain()
        else:
            print("Неизвестная ошибка сервера")

    async def receive(self):
        """Метод получения данных от сервера"""
        while not self.writer.is_closing():
            try:
                message = await self.reader.readuntil(b'\r\n')
                message = message_data.parse_message(message.decode('utf-8', 'ignore'))
                if message['parcelType'] == 'exit':
                    self.print_exit(message)
                elif message['parcelType'] == 'greeting':
                    self.print_greeting(message)
                elif message['parcelType'] == 'message':
                    await self.print_message(message)
                elif message['parcelType'] == 'exception':
                    await self.print_exception(message)
            except (asyncio.IncompleteReadError, ConnectionAbortedError):
                break
            except Exception:
                self.writer.close()
                await self.writer.wait_closed()
                break

    async def send_exit(self):
        self.writer.write(
            f"{{'parcelType':'exit', 'message':'', 'username':'{self.username}', 'attachmentName':'', "
            f"'attachmentSize':'0'}}\r\n".encode('utf-8'))
        await self.writer.drain()
        self.writer.close()
        await self.writer.wait_closed()

    async def send_file(self, message, fp):
        try:
            name, size, file = message_data.load_file(fp)
            self.writer.write(
                f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
                f"'attachmentName':'{name}', 'attachmentSize':'{size}'}}\r\n".encode('utf-8'))
            self.writer.write(file)
            await self.writer.drain()
            print("File sent")
            return True
        except FileNotFoundError:
            print(f"File {fp} not found")

    async def send_message(self, message):
        self.writer.write(
            f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
            f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
        await self.writer.drain()

    async def write(self):
        """Метод отправки данных от клиента"""
        while True:
            try:
                message = await asyncio.get_event_loop().run_in_executor(ThreadPoolExecutor(1), input, "Message\n")
                message = message.replace("\\", "\\\\").replace("'", "\\'")
                if message == "!exit":
                    await self.send_exit()
                    break
                attached = False
                while not attached:
                    fp = await asyncio.get_event_loop().run_in_executor(ThreadPoolExecutor(1), input,
                                                                        "Relative filepath\n")
                    if fp == "!exit":
                        await self.send_exit()
                        break
                    if fp == '':
                        await self.send_message(message)
                        break
                    attached = await self.send_file(message, fp)
            except Exception:
                print("Сервер недоступен")
                self.writer.close()
                await self.writer.wait_closed()
