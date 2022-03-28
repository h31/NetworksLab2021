import socket
import threading
import os
from math import ceil
from random import randint
from setup import *

BLOCK_LENGTH = 512
IP = 'localhost'
PORT = 69
CODE = 'utf-8'
TIMEOUT = 5
MAX_RESEND = 10

ports = []


def ack_package(block):
    package = bytearray()
    package += OPCODES['ack'].to_bytes(2, 'big')
    package += block.to_bytes(2, 'big')
    return package


def data_package(block, data):
    package = bytearray()
    package += OPCODES['data'].to_bytes(2, 'big')
    package += block.to_bytes(2, 'big')
    package += data
    return package


def error_package(code):
    package = bytearray()
    package += OPCODES['error'].to_bytes(2, 'big')
    package += code.to_bytes(2, 'big')
    package += ERROR_CODE[code].encode(CODE)
    package.append(0)
    return package


def random_port():
    while True:
        port = randint(1025, 65536)
        if port not in ports:
            ports.append(port)
            return port


def rrq_command(client_address, file):
    port = random_port()
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    client_socket.bind((IP, port))
    block_number = 1
    try_resend = 1
    input_file = open(file, 'rb')
    data = input_file.read()
    max_block_number = ceil(len(data) / BLOCK_LENGTH)
    while True:
        try:
            data_to_send = data[(block_number - 1) * BLOCK_LENGTH:block_number * BLOCK_LENGTH]
            req = data_package(block_number, data_to_send)
            print(f'Sending {block_number} block, length - {len(data_to_send)} bytes to {client_address}')
            client_socket.sendto(req, client_address)

            client_socket.settimeout(TIMEOUT)
            try:
                recv_data, client_address = client_socket.recvfrom(BLOCK_LENGTH + 4)
            except socket.timeout:
                if try_resend < MAX_RESEND:
                    try_resend += 1
                    continue
                else:
                    print(f'Client: {client_address} not responding')
                    return
            if recv_data:
                opcode = int.from_bytes(recv_data[0:2], 'big')
                if opcode == OPCODES['ack']:
                    recv_block_number = int.from_bytes(recv_data[2:4], 'big')
                    if recv_block_number == max_block_number:
                        print('Whole file sent')
                        input_file.close()
                        ports.remove(port)
                        client_socket.shutdown(socket.SHUT_RDWR)
                        client_socket.close()
                        return
                    if recv_block_number == block_number:
                        block_number += 1
                    elif recv_block_number < block_number:
                        block_number = recv_block_number
                elif opcode == OPCODES['error']:
                    error_code = int.from_bytes(recv_data[2:4], 'big')
                    print(f'Error message from client: {ERROR_CODE[error_code]}')
                    client_socket.shutdown(socket.SHUT_RDWR)
                    client_socket.close()
                    return
        except ConnectionResetError:
            return


def wrq_command(client_address, file):
    client_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    port = random_port()
    client_socket.bind((IP, port))
    block_number = 0
    data = b''
    try_resend = 1
    while True:
        req = ack_package(block_number)
        client_socket.sendto(req, client_address)
        client_socket.settimeout(TIMEOUT)
        try:
            recv_data, client_address = client_socket.recvfrom(BLOCK_LENGTH + 4)
        except socket.timeout:
            if try_resend < MAX_RESEND:
                try_resend += 1
                continue
            else:
                print("Try again. Server not responding")
                return
        if recv_data:
            opcode = int.from_bytes(recv_data[0:2], 'big')
            if opcode == OPCODES['data']:
                recv_block_number = int.from_bytes(recv_data[2:4], 'big')
                if recv_block_number == block_number + 1:
                    data += recv_data[4:]
                    print(f'Received {recv_block_number} block, length - {len(recv_data[4:])} bytes')
                    block_number += 1
                    if len(recv_data[4:]) < BLOCK_LENGTH:
                        print('Whole file received')
                        output_file = open(file, 'wb')
                        output_file.write(data)
                        output_file.close()
                        client_socket.shutdown(socket.SHUT_RDWR)
                        client_socket.close()
                        return
                elif recv_block_number < block_number + 1:
                    req = ack_package(recv_block_number)
                    client_socket.sendto(req, client_address)
            elif opcode == OPCODES['error']:
                error_code = int.from_bytes(recv_data[2:4], 'big')
                print(f'Error message from server: {ERROR_CODE[error_code]}')
                client_socket.shutdown(socket.SHUT_RDWR)
                client_socket.close()
                return


def main():
    print('TFTP server started.')
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    server_socket.bind((IP, PORT))
    while True:
        try:
            recv_data, address = server_socket.recvfrom(BLOCK_LENGTH)
            opcode = int.from_bytes(recv_data[0:2], 'big')
            filename = recv_data[2:].split(b'\x00')[0].decode(CODE)
            recv_mode = recv_data[2:].split(b'\x00')[1].decode(CODE)
            if recv_mode not in MODE:
                request = error_package(4)
                server_socket.sendto(request, address)
                continue
            if opcode == OPCODES['rrq']:
                if not os.path.isfile(filename):
                    request = error_package(1)
                    server_socket.sendto(request, address)
                    continue
                else:
                    threading.Thread(target=rrq_command, args=(address, filename)).start()
            elif opcode == OPCODES['wrq']:
                if os.path.isfile(filename):
                    request = error_package(6)
                    server_socket.sendto(request, address)
                    continue
                else:
                    threading.Thread(target=wrq_command, args=(address, filename)).start()
            else:
                request = error_package(4)
                server_socket.sendto(request, address)
                continue
        except ConnectionResetError:
            print(f'Closed connection from client')
            continue


if __name__ == '__main__':
    main()
