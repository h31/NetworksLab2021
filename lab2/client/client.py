import binascii
import random
import socket
import time

import cache
import message_data


class Client:

    def __init__(self):
        try:
            self.client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
            self.client_socket.connect(('8.8.8.8', 53))

        except TimeoutError:
            print("Server not responding")
            self.client_socket.close()

    def receive(self):
        """Метод получения данных от DNS-сервера"""
        while True:
            try:
                data, _ = self.client_socket.recvfrom(1024)
                message = message_data.parse_response(data)
                print(
                    f"{message['RCODE']}\nResults for {message['QTYPE']}-query {message['QNAME']}: "
                    f"{message['ANCOUNT']}")
                for i in range(1, message['ANCOUNT'] + 1):
                    print(f"{i}.\t{message['ANS'][i - 1]['RDATA']}")
                    cache.add(cache.CacheEntry(message['ANS'][i - 1]['TTL'], time.time(), message))

            except ConnectionAbortedError:
                break
            except Exception:
                self.client_socket.close()
                break

    def write(self):
        """Метод отправки DNS-запроса от клиента
           Запрос представляет собой шестнадцатеричную строку"""
        print("Usage: enter Resource Record Type (A, AAAA, MX, TXT) and domain name")
        while True:
            try:
                RRType = input()
                if RRType in ["A", "AAAA", "MX", "TXT"]:
                    domain_name = input()
                    cache.update()
                    entry = cache.get(RRType, domain_name)
                    if entry:
                        print(
                            f"(cache) Results for {RRType}-query {domain_name}: {len(entry)}")
                        for i in range(1, len(entry) + 1):
                            print(f"{i}.\t{entry[i - 1]['RDATA']}")
                    else:
                        id = '{0:016b}'.format(random.getrandbits(16))
                        message = message_data.build_request(id, RRType, domain_name)
                        self.client_socket.send(binascii.unhexlify(message))
                else:
                    print("Incorrect type")
            except Exception:
                print("Server is not available")
                break
