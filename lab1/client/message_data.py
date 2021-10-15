import datetime
import os


def load_file(fp):
    with open(fp, 'rb') as file:
        data = file.read()
    return os.path.basename(fp), os.path.getsize(fp), data


def parse_message(message):
    res = []
    i = 0
    while i < len(message):
        print(message[i])
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
    try:
        with open(f"./Downloads/{username}'s {name}", 'wb+') as filename:
            filename.write(data)
    except OSError:
        print("An error occurred while saving the file")


def time_format(minsec):
    min, sec = minsec.split(':')[1:]
    return datetime.datetime.today().replace(minute=int(min), second=int(sec)).strftime("%Y-%m-%d %H:%M:%S")
