import pickle

import matplotlib.pyplot as plt


def encode_message(message_type, sender, text, fp=None):
    message = {"type": message_type, "sender": sender, "text": text}
    if message_type == "client message with file" and fp is not None:
        file = open(fp, 'r')
        data = plt.imread(file)
        message['filename'] = file
        message['attachment'] = data.encode('base64')
        file.close()
    return text.encode('utf-8')


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
