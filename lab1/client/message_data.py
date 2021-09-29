import os
import pickle
import time


def encode_message(message_type, username, text, fp=None):
    message = {"type": message_type, "username": username, "text": text}
    if message_type == "client message with file" and fp is not None:
        file = open(fp, 'rb')
        message['attachmentType'] = '.' + fp.split('.')[-1]
        message['file'] = file
        file.close()
    return message


def load_file(fp):  # TODO
    file = open(fp, 'rb')
    data = file.read()
    file.close()
    return os.path.getsize(fp), data


def decode_message(message):
    message = pickle.loads(message)
    if message['parcelType'] == "client message with file":
        data = message['attachment'].decode('base64')
        file = open(
            f"./Downloads/{message['time'].strftime('%d%M%Y%H%m')} {message['filename']} by {message['nickname']}.jpg",
            'w')
        file.write(data)
        file.close()
        return message['type'], message['sender'], message['time'], message['text'], message['filename']
    else:
        return message['type']


def save_file(nickname, data, extension):
    filename = ''
    try:
        filename = open(
            f"./Downloads/{time.strftime('%d%M%Y%H%m%s', time.localtime())} attachment by {nickname}{extension}", 'wb+')
        filename.write(data)
        filename.close()
    except OSError:
        print("An error occurred while saving the file")
    return filename
