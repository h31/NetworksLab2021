import socket
import threading
import os

from clientConnection import Connection

sock = socket.socket()

address = input(f'Введите адрес, если вы хотите подключиться к серверу на удалённом компьютере. \nЕсли вы хотите '
                f'подключиться к серверу на локальном компьютере, нажмите enter:')

if address == '':
    address = 'localhost'

sock.connect((address, 12345))
disconnect_event = threading.Event()
sock_connection = Connection(sock, disconnect_event)

username_limit = 20
username = input(f'Введите имя (максимальное кол-во символов - {username_limit} ):').encode()

while True:
    if len(username) > username_limit:
        username = input(f'Имя слишком длинное, введите другое').encode()
    else:
        reg_result = sock_connection.registration(username)
        if not reg_result:
            username = input(f'Это имя занято, введите другое:').encode()
        else:
            print('Добро пожаловать в чат,', username.decode(),
                  '!\nЧтобы отправить сообщение, введите его в консоль.\n'
                  'Если вы хотите прикрепить файл, то формат ввода следующий: ваше сообщение & путь к файлу.\n'
                  'Чтобы выйти из чата, отправьте q.')
            break

listenThread = threading.Thread(target=sock_connection.recvAndPrint)
listenThread.start()

while True:
    message = input()
    if sock_connection.disconnect_event.is_set():  # если сервер отключился
        break
    if ' & ' in message:
        message_parts = message.split(' & ')
        message = message_parts[0]
        file_path = message_parts[1]
        if not os.path.isfile(file_path):
            print('Проверьте корректность ввода сообщения, система не нашла файл')
            continue
    else:
        file_path = None
    sock_connection.sendToServer(message.encode(), file_path)

sock.close()
listenThread.join()
