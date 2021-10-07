import datetime
import os


def load_file(fp):
    file = open(fp, 'rb')
    file_attributes = os.path.basename(fp).split('.')
    data = file.read()
    file.close()
    return file_attributes[0], file_attributes[-1], os.path.getsize(fp), data


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
