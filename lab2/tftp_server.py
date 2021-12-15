import random
import os.path
import socket

from tftp_info import TFTP_OPCODES, TFTP_SERVER_ERRORS, CLIENT_COMMANDS


SERVER_ADDRESS = 'localhost'
BLOCKSIZE = 512
TIMEOUT = 10


def server():
    print('Server is working')
    listen_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    listen_socket.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    listen_socket.bind((SERVER_ADDRESS, 69))
    while True:
        data, client = listen_socket.recvfrom(BLOCKSIZE + 4)
        print('data size', len(data))
        print('Client address -', client)
        opcode = data[1]
        if opcode == TFTP_OPCODES['read']:
            read_request(data, client)
        elif opcode == TFTP_OPCODES['write']:
            write_request(data, client)


def read_request(data, client):
    current_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    current_socket.bind((SERVER_ADDRESS, random.randint(1024, 65535)))

    exp_ack_num = 1
    block_num_to_send = 0
    filename, exist_file = check_file(data)
    limit_to_recv = 5
    last_block = False

    file = open(filename, 'rb')
    if not exist_file:
        print(f'Error (code : 1)\n'
              f'Message: {TFTP_SERVER_ERRORS[1]}')
        error_to_send = error_msg(1)
        current_socket.sendto(error_to_send, client)
        return

    while True:
        block_to_send = file.read(512)
        if len(block_to_send) < 512:
            last_block = True
        data_to_send = data_msg(block_to_send, exp_ack_num)
        current_socket.sendto(data_to_send, client)

        current_socket.settimeout(TIMEOUT)
        try:
            data, client = current_socket.recvfrom(BLOCKSIZE + 4)
        except socket.timeout:
            if limit_to_recv != 0:
                limit_to_recv = limit_to_recv - 1
                continue
            else:
                file.close()
                print('Connection terminated')
                current_socket.close()
                return
        if data is not None:
            opcode = data[1]
            if opcode == TFTP_OPCODES['ack']:
                recv_ack_num = 256 * data[2] + data[3]
                if last_block:
                    file.close()
                    print('File has been sent')
                    current_socket.close()
                    return
                if recv_ack_num == exp_ack_num:
                    exp_ack_num += 1
                    block_num_to_send += 1
                    continue
                elif recv_ack_num < exp_ack_num:
                    continue
            elif opcode == TFTP_OPCODES['error']:
                file.close()
                err_code = data[3]
                print(f'Error (code : {err_code})\n'
                      f'Message: {TFTP_SERVER_ERRORS[err_code]}')
                current_socket.close()
                return


def write_request(data, client):
    current_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    current_socket.bind((SERVER_ADDRESS, random.randint(1024, 65535)))

    filename, exist_file = check_file(data)

    if exist_file:
        print(f'Error (code : 6)\n'
              f'Message: {TFTP_SERVER_ERRORS[6]}')
        error_to_send = error_msg(6)
        current_socket.sendto(error_to_send, client)
        return

    recv_block = b''
    exp_block_num = 0
    limit_to_recv = 5

    while True:
        msg = ack_msg(exp_block_num)
        current_socket.sendto(msg, client)
        exp_block_num += 1

        current_socket.settimeout(TIMEOUT)
        try:
            data, client = current_socket.recvfrom(BLOCKSIZE + 4)
        except socket.timeout:
            if limit_to_recv != 0:
                limit_to_recv = limit_to_recv - 1
                continue
            else:
                print('Connection terminated')
                current_socket.close()
                return

        if data is not None:
            opcode = data[1]

            if opcode == TFTP_OPCODES['data']:
                recv_block_num = 256 * data[2] + data[3]

                if recv_block_num < exp_block_num:
                    continue
                elif exp_block_num == recv_block_num:
                    block_data = data[4:]
                    recv_block += block_data
                    if len(block_data) < BLOCKSIZE:
                        print('File has been received')
                        msg = ack_msg(exp_block_num)
                        current_socket.sendto(msg, client)
                        with open(filename, 'wb') as file:
                            file.write(recv_block)
                        current_socket.close()
                        return
                    continue

            elif opcode == TFTP_OPCODES['error']:
                error_code = data[3]
                print(f'Error (code : {error_code})\n'
                      f'Message: {TFTP_SERVER_ERRORS[error_code]}')
                current_socket.close()
                return


def check_file(data):
    filename = (data[2:(len(data)-7)]).decode()
    return filename, os.path.isfile(filename)


def error_msg(err_code):
    msg = bytearray()
    opcode_bytes = TFTP_OPCODES['error'].to_bytes(2, byteorder='big')
    msg[0:0] = opcode_bytes
    err_code_bytes = err_code.to_bytes(2, byteorder='big')
    msg[2:2] = err_code_bytes
    err_msg_bytes = bytearray(TFTP_SERVER_ERRORS[err_code].encode('utf-8'))
    msg[4:4] = err_msg_bytes
    msg.append(0)
    return msg


def data_msg(data, block):
    msg = bytearray()
    opcode_bytes = TFTP_OPCODES['data'].to_bytes(2, byteorder='big')
    msg[0:0] = opcode_bytes
    block_bytes = block.to_bytes(2, byteorder='big')
    msg[2:2] = block_bytes
    msg[4:4] = data
    return msg


def ack_msg(block):
    msg = bytearray()
    opcode_bytes = TFTP_OPCODES['ack'].to_bytes(2, byteorder='big')
    msg[0:0] = opcode_bytes
    block_bytes = block.to_bytes(2, byteorder='big')
    msg[2:2] = block_bytes
    return msg


server()