import datetime
import os


def load_file(fp):
    """
    Метод загрузки файла от клиента-отправителя
    :param fp: путь до файла у клиента - отправителя сообщения
    :type fp: str
    """
    with open(fp, 'rb') as file:
        data = file.read()
    return os.path.basename(fp), os.path.getsize(fp), data


def parse_message(message):
    """
    Парсер сообщений от сервера
    :param message: сообщение от сервера
    :type message: str
    """
    res = []
    i = 0
    while i < len(message):
        if message[i] == '\'':
            entry = ''
            j = i + 1
            while j < len(message):
                if message[j] == '\\' and message[j + 1] == '\\' or message[j] == '\\' and message[j + 1] == '\'':
                    entry += message[j]
                    entry += message[j + 1]
                    j += 2
                    continue
                if message[j] == '\'':
                    res.append(entry)
                    i = j + 1
                    break
                entry += message[j]
                j += 1
        i += 1
    return dict(zip(res[::2], res[1::2]))


def save_file(username, name, data):
    """
    Метод сохранения файла от сервера для клиента-получателя
    :param username: имя пользователя - отправителя файла
    :type username: str
    :param name: имя файла
    :type name: str
    :param data: содержимое файла
    :type data: byte
    """
    try:
        with open(f"./Downloads/{username}'s {name}", 'wb+') as filename:
            filename.write(data)
    except OSError:
        print("An error occurred while saving the file")


def time_format(minsec):
    """
    Метод преобразования времени от сервера для локального клиента
    :param minsec: время от сервера
    :type minsec: str
    """
    min, sec = minsec.split(':')[1:]
    return datetime.datetime.today().replace(minute=int(min), second=int(sec)).strftime("%Y-%m-%d %H:%M:%S")


save_file('test', 'mabitch.mp4', load_file('../Sbor.mp4')[-1])
