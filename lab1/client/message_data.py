def encode_message(message_type, sender, text, fp):
    message = {"type": message_type, "sender": sender, "text": text}
    if message_type == "Client message":
        file = open(fp, 'r')
        data = file.read()
        message['filename'] = file
        message['attachment'] = data.encode('base64')
    return message


def decode_message(message):
    if message['type'] == "Client message":
        data = message['attachment'].decode('base64')
        file = open(
            f"./Downloads/{message['time'].strftime('%d%M%Y%H%m')} {message['filename']} by {message['sender']}.jpg",
            'w')
        file.write(data)
        file.close()
        return message['type'], message['sender'], message['time'], message['text'], message['filename']
    else:
        return message['type']
