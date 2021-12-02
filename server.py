import asyncio
from datetime import datetime
from pytz import timezone

code_table = 'utf-8'

hostS = 'networkslab-ivt.ftp.sh'
hostL = '127.0.0.1'
port = 55555
file_end = '37e3f4a8-b8c9-4f22-ad4d-8bd81e686822'
length_of_message = len(f"file{file_end}")

writers = []

def broadcast(message, user):
    for client in writers:
        if client != user:
            client.write(message)


async def handle(reader, writer):
    writers.append(writer)
    buffer = 0
    full_mes = bytes()
    full_file = bytes()
    while True:
        # try:
        if buffer == 0:
            message = await reader.read(length_of_message)
            message = message.decode(code_table)
            if message.strip().isdigit():  # если число, то это просто сообщение и нужно изменить длину буфера
                buffer = int(message.strip())
            if message == f"file{file_end}":  # если file, то хотят отправить файл
                file_header_len = await reader.read(length_of_message)
                file_header_len = int(file_header_len.decode(code_table))
                file_name_size = await reader.read(file_header_len)  # заголовок это имя файла и размер файла
                file_name_size = file_name_size.decode(code_table)
                file_size = file_name_size[file_name_size.find("<>") + 2:]
                file_size = int(file_size)
                file_data_read = await reader.read(file_size)
                full_file += file_data_read
                while not file_end.encode(code_table) in full_file:
                    file_data_read = await reader.read(file_size)
                    full_file += file_data_read
                else:
                    broadcast(f'{f"file{file_end}":<{len(f"file{file_end}".encode(code_table))}}'.encode(code_table) +
                              f'{len(file_name_size.encode(code_table)):<{length_of_message}}'.encode(
                                  code_table) +
                              file_name_size.encode(code_table) + full_file, writer)
                    print("serF2")
        else:
            message = await reader.read(buffer)
            full_mes += message
            # это часть модернизируется с целью успешного получения всего сообщения при выкладке сервера удаленно
            while not file_end.encode(code_table) in full_mes:
                message = await reader.read(buffer)
                full_mes += message
            else:
                time_zone = full_mes[
                            full_mes.find('<'.encode(code_table)) + 1: full_mes.find('>'.encode(code_table))]
                now_time = datetime.now(timezone(time_zone)).strftime(
                    "%Y-%m-%d %H:%M")  # время сервера измененное в соответствии с tz пользователя
                message_send = '<'.encode(code_table) + now_time.encode(code_table) + '> '.encode(code_table) \
                               + full_mes[full_mes.find('>'.encode(code_table)) + 1:]
                message_len_send = f'{len(message_send):<{length_of_message}}'.encode(
                    code_table)
                broadcast(message_len_send + message_send, writer)
                print("sev2")
                buffer = 0

async def receive_connection():
    while True:
        server = await asyncio.start_server(
            handle, hostL, port)
        addr = server.sockets[0].getsockname()
        print(f'Serving on {addr}')
        async with server:
            await server.serve_forever()


asyncio.run(receive_connection())