import binascii
import random
import socket

import message_data


class Client:

    def __init__(self):
        try:
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self.client_socket.connect(('8.8.8.8', 53))
            self.ids = []

        except TimeoutError:
            print("Сервер не отвечает")
            self.client_socket.close()

    def receive(self):
        """Метод получения данных от DNS-сервера"""
        while True:
            try:
                data, _ = self.client_socket.recvfrom(4096)
                print(message_data.format_hex(binascii.hexlify(data).decode("utf-8")))
                message = message_data.parse_message(data)
                if message['id'] in self.ids:
                    # TODO: вот тут смотрим на сообщение, парсим нормально и отображаем результаты
                    self.ids.remove(message['id'])  # после успешной обработки удаляем id запроса
                    pass
                else:
                    continue
                if message['parcelType'] == 'exit':
                    print(f"\n[{message['time']}] {message['username']} покинул чат")
                elif message['parcelType'] == 'greeting':
                    print(f"\n[{message['time']}] {message['username']} вошел в чат")
                elif message['parcelType'] == 'message':
                    print(
                        f"\n[{message['time']}] {message['username']} сказал: {message['message']}")
                else:
                    print("Неизвестная ошибка сервера")
            except ConnectionAbortedError:
                break
            except Exception:
                self.client_socket.close()
                break

    def write(self):
        """Метод отправки DNS-запроса от клиента
           Запрос представляет собой шестнадцатеричную строку"""
        while True:
            try:
                RRType = input("Enter Resource Record Type: A, AAAA, MX, TXT\n")
                if RRType in ["A", "AAAA", "MX", "TXT"]:
                    URL = input("Enter URL: ")
                    id = '{0:016b}'.format(random.getrandbits(16))
                    self.ids.append(id)
                    message = message_data.build_request(id, RRType, URL)
                    self.client_socket.send(binascii.unhexlify(message))
                else:
                    print("Incorrect type")

            except Exception:
                print("Сервер недоступен")
                break
