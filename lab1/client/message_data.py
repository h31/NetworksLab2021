import datetime
import os


def load_file(fp):
    file = open(fp, 'rb')
    file_attributes = os.path.basename(fp).split('.')
    data = []
    size = os.path.getsize(fp)
    for _ in range(size):
        data.append(file.read(1))
    file.close()
    return file_attributes[0], file_attributes[-1], size, data


def save_file(nickname, extension, name, data):
    try:
        filename = open(
            f"./Downloads/{name} (by {nickname}).{extension}", 'wb+')
        filename.write(data)
        filename.close()
    except OSError:
        print("An error occurred while saving the file")


def time_format(minsec):
    min, sec = minsec.split(':')[1:]
    return datetime.datetime.today().replace(minute=int(min), second=int(sec)).strftime("%Y-%m-%d %H:%M:%S")
