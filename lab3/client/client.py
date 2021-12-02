import asyncio
import traceback

import message_data


class Client:

    def __init__(self, event_loop):
        self.reader, self.writer = [None, None]
        self.loop = event_loop
        self.logged = False
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

    async def receive(self):
        """Метод получения данных от сервера"""
        while True:
            try:
                message = bytearray()
                letter = await self.reader.read(1)
                while letter != b'\r':
                    message += letter
                    letter = await self.reader.read(1)
                await self.reader.read(1)
                message = message.decode('utf-8', 'ignore')
                message = message_data.parse_message(message)
                if message['parcelType'] == 'exit':
                    print(f"\n[{message_data.time_format(message['time'])}] {message['username']} покинул чат")
                elif message['parcelType'] == 'greeting':
                    print(f"\n[{message_data.time_format(message['time'])}] {message['username']} вошел в чат")
                    if message['username'] == self.username:
                        self.logged = True
                        print("Если тебе надоест общение, напиши !exit\n")
                elif message['parcelType'] == 'message':
                    print(
                        f"\n[{message_data.time_format(message['time'])}] {message['username']}: {message['message']}")
                    if message['attachmentSize'] != '0' and message['username'] != self.username:
                        size = int(message['attachmentSize'])
                        count = size
                        attachment = bytearray()
                        while count > 0:
                            attachment += await self.reader.read(count)
                            count = size - len(attachment)
                        message_data.save_file(message['username'], message['attachmentName'], attachment)
                        print(f"Received {message['attachmentName']} from {message['username']}")
                elif message['parcelType'] == 'exception':
                    if message['message'] == '1':
                        print("Имя пользователя уже занято!\n")
                        self.set_username("Username:")
                        self.writer.write(
                            f"{{'parcelType':'greeting', 'message':'', 'username':'{self.username}', "
                            f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                        await self.writer.drain()
                    else:
                        print("Неизвестная ошибка сервера")
            except ConnectionAbortedError:
                print(traceback.format_exc())
                self.loop.stop()
                break
            except Exception:
                print(traceback.format_exc())
                self.writer.close()
                await self.writer.wait_closed()
                self.loop.stop()
                break

    async def write(self):
        """Метод отправки данных от клиента"""
        while True:
            if self.username != '' and self.logged:
                try:
                    await asyncio.sleep(0.01)
                    message = input("Message\n").replace("\\", "\\\\").replace("'", "\\'")
                    await asyncio.sleep(0.01)
                    if message == "!exit":
                        self.writer.write(
                            f"{{'parcelType':'exit', 'message':'', 'username':'{self.username}', 'attachmentName':'', "
                            f"'attachmentSize':'0'}}\r\n".encode('utf-8'))
                        await self.writer.drain()
                        self.writer.close()
                        await self.writer.wait_closed()
                        self.logged = False
                        self.loop.stop()
                        break
                    attached = False
                    while not attached:
                        fp = input("Relative filepath\n")
                        await asyncio.sleep(0.01)
                        if fp == "!exit":
                            self.writer.write(
                                f"{{'parcelType':'exit', 'message':'', 'username':'{self.username}', "
                                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                            await self.writer.drain()
                            self.writer.close()
                            await self.writer.wait_closed()
                            self.logged = False
                            break
                        if fp == '':
                            self.writer.write(
                                f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
                                f"'attachmentName':'', 'attachmentSize':'0'}}\r\n".encode('utf-8'))
                            await self.writer.drain()
                            break
                        try:
                            name, size, file = message_data.load_file(fp)
                            self.writer.write(
                                f"{{'parcelType':'message', 'message':'{message}', 'username':'{self.username}', "
                                f"'attachmentName':'{name}', 'attachmentSize':'{size}'}}\r\n".encode('utf-8'))
                            self.writer.write(file)
                            await self.writer.drain()
                            print("File sent")
                            attached = True
                        except FileNotFoundError:
                            print(traceback.format_exc())
                            print(f"File {fp} not found")
                except Exception:
                    print("Сервер недоступен")
                    print(traceback.format_exc())
                    self.loop.stop()
                    break
            else:
                await asyncio.sleep(0.01)
