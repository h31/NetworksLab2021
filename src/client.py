import socket
import os
from setup import *

BLOCK_LENGTH = 512
IP = "localhost"
PORT = 69
CODE = 'utf-8'
TIMEOUT = 5
MAX_RESEND = 10



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


def rrq_command(sock, file):
    block_number = 1
    data = b''
    try_resend = 1
    while True:
        sock.settimeout(TIMEOUT)
        try:
            recv_data, address = sock.recvfrom(BLOCK_LENGTH + 4)
        except socket.timeout:
            if block_number == 1:
                print("Try again. Server not responding")
                return
            if try_resend < MAX_RESEND:
                try_resend += 1
                req = ack_package(block_number)
                sock.sendto(req, address)
                continue
            else:
                print("Try again. Server not responding")
                return

        if recv_data:
            opcode = int.from_bytes(recv_data[0:2], 'big')
            if opcode == OPCODES['data']:
                recv_block_number = int.from_bytes(recv_data[2:4], 'big')
                if recv_block_number == block_number:
                    data += recv_data[4:]
                    print(f'Received {block_number} block, length - {len(recv_data[4:])} bytes')
                    req = ack_package(block_number)
                    sock.sendto(req, address)
                    block_number += 1
                    if len(recv_data[4:]) < BLOCK_LENGTH:
                        print('Whole file received')
                        output_file = open(file, 'wb')
                        output_file.write(data)
                        output_file.close()
                        sock.shutdown(socket.SHUT_RDWR)
                        sock.close()
                        return
                elif recv_block_number < block_number:
                    req = ack_package(recv_block_number)
                    sock.sendto(req, address)
            elif opcode == OPCODES['error']:
                error_code = int.from_bytes(recv_data[2:4], 'big')
                print(f'Error message from server: {ERROR_CODE[error_code]}')
                sock.shutdown(socket.SHUT_RDWR)
                sock.close()
                return


def wrq_command(sock, file):
    block_number = 0
    try_resend = 1
    input_file = open(file, 'rb')
    data = input_file.read(512)
    while True:

        sock.settimeout(TIMEOUT)
        try:
            recv_data, address = sock.recvfrom(BLOCK_LENGTH)
        except socket.timeout:
            if block_number == 0:
                print("Try again. Server not responding.")
                return
            if try_resend < MAX_RESEND:
                try_resend += 1
                req = data_package(block_number, data)
                sock.sendto(req, address)
                continue
            else:
                print("Try again. Server not responding")
                return

        if recv_data:
            opcode = int.from_bytes(recv_data[0:2], 'big')
            if opcode == OPCODES['ack']:
                recv_block_number = int.from_bytes(recv_data[2:4], 'big')
                if recv_block_number == block_number:
                    data_to_sent = data
                    block_number += 1
                    data = input_file.read(512)
                    print(f'Sending {block_number} block, length - {len(data_to_sent)} bytes')
                    req = data_package(block_number, data_to_sent)
                    sock.sendto(req, address)
                    if len(data_to_sent) < BLOCK_LENGTH:
                        print('Whole file sent')
                        input_file.close()
                        sock.shutdown(socket.SHUT_RDWR)
                        sock.close()
                        return
                elif recv_block_number < block_number:
                    req = data_package(recv_block_number,
                                       data[recv_block_number * BLOCK_LENGTH:(recv_block_number + 1) * BLOCK_LENGTH])
                    sock.sendto(req, address)
            elif opcode == OPCODES['error']:
                error_code = int.from_bytes(recv_data[2:4], 'big')
                print(f'Error message from server: {ERROR_CODE[error_code]}')
                sock.shutdown(socket.SHUT_RDWR)
                sock.close()
                return


def main():
    print('TFTP client started.')
    while True:
        print('Available commands are "get" and "put".\nCommand format: get/put filename.\n'
              'Print !exit for exit from TFTP client.')
        try:
            command = input().split()
            i = 1
            length = len(command)
            if command:
                opcode = command[0]
                if opcode == '!exit':
                    print("Goodbye")
                    return
                if (opcode != 'get' and opcode != 'put') or length != 2:
                    print('Your command is wrong')
                    continue
                filename = command[1]
                if opcode == 'put' and not os.path.isfile(filename):
                    print('No such file. Try again')
                    continue
                while True:
                    if opcode == 'get' and os.path.isfile(filename):
                        filename = command[1].split('.')[0] + '_' + str(i) + '.' + command[1].split('.')[1]
                        i += 1
                    else:
                        break
                request = bytearray()
                if opcode == 'get':
                    request += OPCODES['rrq'].to_bytes(2, 'big')
                else:
                    request += OPCODES['wrq'].to_bytes(2, 'big')
                request += command[1].encode(CODE)
                request.append(0)
                request += MODE['octet'].encode(CODE)
                request.append(0)
                client_sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
                server_address = (IP, PORT)
                client_sock.sendto(request, server_address)
                try:
                    if opcode == 'get':
                        rrq_command(client_sock, filename)
                    else:
                        wrq_command(client_sock, filename)
                except ConnectionResetError:
                    print('Closed connection')
                    client_sock.shutdown(socket.SHUT_RDWR)
                    client_sock.close()
                    return
        except KeyboardInterrupt:
            return


if __name__ == '__main__':
    main()
