import socket
import os.path

from tftp_info import TFTP_OPCODES, TFTP_SERVER_ERRORS, CLIENT_COMMANDS

SERVER_ADDRESS = 'localhost'
BLOCK_SIZE = 512
TIMEOUT = 10
TFTP_MODE = 'octet'


def client():
    print('Добро пожаловать!')
    while True:
        print('Выберите одну из команд:\nRD - чтение файла\nWR - запись файла\nQ - выход из программы')
        while True:
            command = input()
            if command not in CLIENT_COMMANDS:
                print('Неверная команда, введите другую!')
                continue
            elif command == 'Q':
                return
            else:
                break

        while True:
            filename = input('Введите название файла:')
            if command == 'RD' and os.path.isfile(filename):
                print('Файл с таким именем уже существует')
                continue
            elif command == 'WR' and not os.path.isfile(filename):
                print('Нет файла с таким именем')
                continue
            else:
                break

        init_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        server_address = (SERVER_ADDRESS, 69)
        request = init_request(CLIENT_COMMANDS[command], filename, TFTP_MODE)
        init_socket.sendto(request, server_address)

        if command == 'RD':
            read_request(init_socket, filename)
        elif command == 'WR':
            write_request(init_socket, filename)


def read_request(current_socket, filename):
    recv_block = b''
    exp_block_num = 1

    while True:
        current_socket.settimeout(TIMEOUT)
        try:
            data, server = current_socket.recvfrom(BLOCK_SIZE + 4)
        except socket.timeout:
            if exp_block_num == 1:
                print('Нет соединения с сервером')
                exit(1)
            else:
                try:
                    msg = ack_msg(exp_block_num)
                    current_socket.sendto(msg, client)
                    continue
                except:
                    print('Сервер отключился')
                    exit(1)

        if data is not None:
            opcode = data[1]

            if opcode == TFTP_OPCODES['data']:
                recv_block_num = 256 * data[2] + data[3]

                if recv_block_num < exp_block_num:
                    msg = ack_msg(recv_block_num)
                    current_socket.sendto(msg, server)
                    continue
                elif exp_block_num == recv_block_num:
                    recv_data = data[4:]
                    recv_block += recv_data
                    msg = ack_msg(exp_block_num)
                    current_socket.sendto(msg, server)
                    exp_block_num += 1
                    if len(recv_data) < BLOCK_SIZE:
                        size = len(recv_data) + (recv_block_num - 1) * 512
                        print(f'Скачивание файла завершено успешно, получен(о) {size} байт')
                        with open(filename, 'wb') as file:
                            file.write(recv_block)
                        current_socket.close()
                        return
                    continue
            elif opcode == TFTP_OPCODES['error']:
                err_code = data[3]
                print(f'Ошибка (код : {err_code})\n'
                      f'Сообщение: {TFTP_SERVER_ERRORS[err_code]}')
                current_socket.close()
                return


def write_request(current_socket, filename):
    exp_ack_num = 1
    block_num_to_send = 0
    last_block = False

    file = open(filename, 'rb')
    while True:
        current_socket.settimeout(TIMEOUT)
        try:
            data, server = current_socket.recvfrom(BLOCK_SIZE + 4)
        except socket.timeout:
            if exp_ack_num == 1:
                file.close()
                print('Нет соединения с сервером')
                exit(1)
            else:
                try:
                    data_to_send = data_msg(block_to_send, exp_ack_num)
                    current_socket.sendto(data_to_send, server)
                except:
                    file.close()
                    print('Сервер отключился')
                    exit(1)

        if data is not None:
            opcode = data[1]

            if opcode == TFTP_OPCODES['ack']:
                received_ack_block_num = 256 * data[2] + data[3]
                if last_block:
                    size = (received_ack_block_num - 1) * 512 + len(block_to_send)
                    print('Отправка данных завершена, было отправлен(о)', size, 'байт')
                    current_socket.close()
                    return
                elif received_ack_block_num == exp_ack_num:
                    exp_ack_num += 1
                    block_num_to_send += 1

            elif opcode == TFTP_OPCODES['error']:
                error_code = data[3]
                print(f'Ошибка (код : {error_code})\n'
                      f'Сообщение: {TFTP_SERVER_ERRORS[error_code]}')
                current_socket.close()
                return

        block_to_send = file.read(512)
        if len(block_to_send) < 512:
            last_block = True
        data_to_send = data_msg(block_to_send, exp_ack_num)
        current_socket.sendto(data_to_send, server)


def init_request(req_type, filename, mode):
    request = bytearray()
    if req_type == TFTP_OPCODES['read']:
        opcode_bytes = TFTP_OPCODES['read'].to_bytes(2, byteorder='big')
    elif req_type == TFTP_OPCODES['write']:
        opcode_bytes = TFTP_OPCODES['write'].to_bytes(2, byteorder='big')
    request[0:0] = opcode_bytes
    filename = bytearray(filename.encode('utf-8'))
    request += filename
    request.append(0)
    mode = bytearray(bytes(mode, 'utf-8'))
    request += mode
    request.append(0)
    return request


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


client()
