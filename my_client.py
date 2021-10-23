import os
import socket
import threading
from tzlocal import get_localzone

code_table = 'utf-8'
length_of_message = 8
name = input("Username. No more then 16 symbols: ")
if len(name) > 16:
    print("No more then 16 symbols, try again")
    nickname = input("Username: ")

hostS = 'networkslab-ivt.ftp.sh'
hostL = '127.0.0.1'
port = 55555
file_end = '<end_file>'

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect((hostS, 55555))


def receive_message():
    buffer = 0
    full_mes = bytes()
    full_file = bytes()
    while True:
        if buffer == 0:
            message = client.recv(length_of_message)
            if message.strip().isdigit():
                buffer = int(message.strip())
            if message.strip() == "file~~".encode(code_table):
                file_header_len = int(client.recv(length_of_message).decode())
                file_name_size = client.recv(file_header_len).decode(code_table)
                file_name = file_name_size[:file_name_size.find("<>")]
                file_size = file_name_size[file_name_size.find("<>") + 2:]
                file_name = os.path.basename(file_name)
                file_size = int(file_size)
                f = open("send" + file_name, "wb")
                file_data_write = client.recv(file_size)
                full_file += file_data_write
                while not file_end.encode(code_table) in full_file:
                    file_data_write = client.recv(file_size)
                    full_file += file_data_write
                else:
                    f.write(full_file[:full_file.find(file_end.encode(code_table))])
                    f.close()
                print(f'File {file_name} is received ')
        else:
            message = client.recv(buffer)
            full_mes += message
            while not "~~".encode(code_table) in full_mes:
                message = client.recv(buffer)
                full_mes += message
            else:
                print(full_mes[:full_mes.find("~~".encode(code_table))].decode(code_table))
                buffer = 0


def send_message():
    while True:
        local_tz = get_localzone()
        message = input("") + '~~'
        if message == "file~~":
            file_name = input("Type file name: ")
            file_size = os.path.getsize(file_name) + len(file_end.encode(code_table))
            client.send(f"{message}".encode(code_table))
            file_name_size = f"{file_name}<>{file_size}".encode()
            client.send(f"{len(file_name_size):<{length_of_message}}".encode(code_table))
            client.send(file_name_size)
            f = open(file_name, "rb")
            file_data_write = f.read(file_size)
            mod_data_to_send = file_data_write + file_end.encode(code_table)
            client.send(mod_data_to_send)
            f.close()
            print(f'File {file_name} is send')
        else:
            message_send = f'<{local_tz}>{name}->{message}'.encode(code_table)
            message_len_send = f'{len(message_send):<{length_of_message}}'.encode(code_table)
            client.send(message_len_send + message_send)


receive_thread = threading.Thread(target=receive_message)
receive_thread.start()

write_thread = threading.Thread(target=send_message)
write_thread.start()
