import socket
import threading

from clientConnection import Connection


sock = socket.socket()
#sock.connect(('networkslab-ivt.ftp.sh', 12345))
sock.connect(('localhost', 12345))
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
    if sock_connection.disconnect_event.is_set(): # если сервер отключился
        break
    if ' & ' in message:
        message_parts = message.split(' & ')
        message = message_parts[0]
        file_path = message_parts[1]
    else:
        file_path = None
    sock_connection.sendToServer(message.encode(), file_path)

sock.close()
listenThread.join()