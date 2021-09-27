import pickle
import time


def encode_message(message_type, sender, text, fp=None):
    message = {"type": message_type, "sender": sender, "text": text}
    if message_type == "client message with file" and fp is not None:
        file = open(fp, 'rb')
        message['file_extension'] = '.' + fp.split('.')[-1]
        message['file'] = file
        file.close()
    return message


def load_file(fp):  # TODO
    res = []
    file = open(fp, 'rb')
    res.append = '.' + fp.split('.')[-1]
    res.append = file
    file.close()
    return res


def decode_message(message):
    message = pickle.loads(message)
    if message['type'] == "client message with file":
        data = message['attachment'].decode('base64')
        file = open(
            f"./Downloads/{message['time'].strftime('%d%M%Y%H%m')} {message['filename']} by {message['sender']}.jpg",
            'w')
        file.write(data)
        file.close()
        return message['type'], message['sender'], message['time'], message['text'], message['filename']
    else:
        return message['type']


def save_file(nickname, file, extension):  # TODO
    filename = open(
        f"./Downloads/{time.strftime('%d%M%Y%H%m', time.localtime())} attachment by {nickname}{extension}", 'wb')
    filename.write(file)
    filename.close()
    return filename
