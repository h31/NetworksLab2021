from message_id import MessageId


def decode(b):
    decode_arr = []
    byte_arr = split(b[10:])
    if byte_arr[0] is MessageId.error_message.value:
        return byte_arr
    id_msg = MessageId(byte_arr[0].decode('utf-8'))
    for i in range(len(byte_arr)):
        if id_msg.isNextFile(i):
            decode_arr.append(byte_arr[-1])
        else:
            decode_arr.append(byte_arr[i].decode('utf-8').replace('##', '#').replace('#;', ';'))
    return decode_arr


def split(b):
    id_msg = MessageId(chr(b[0]))
    byte_arr = []
    i = 0
    start = 0
    while i < len(b):
        if id_msg.isNextFile(len(byte_arr)):
            byte_arr.append(b[start:])
            return byte_arr
        if b[i] == ord('#'):
            if i == len(b) - 1:
                return ['0', 'Unexpected use of #.']
            if b[i + 1] in [ord('#'), ord(';')]:
                i += 2
                continue
            else:
                return ['0', 'Unexpected use of #.']
        if b[i] == ord(';'):
            byte_arr.append(b[start:i])
            start = i + 1
        i += 1
    byte_arr.append(b[start:])
    return byte_arr


def encode(arr):
    id_msg = MessageId(arr[0])
    arr_len = len(arr)
    b = bytearray()
    msg_len = 0
    for i in range(arr_len):
        if (i == arr_len - 1) and id_msg.withFile():
            b.extend(arr[-1])
            msg_len = msg_len + len(arr[-1])
            break
        if id_msg.fromClient():
            chunk = arr[i].encode('utf-8') + b';'
            b.extend(chunk)
        else:
            chunk = arr[i].replace('#', '##').replace(';', '#;').encode('utf-8') + b';'
            b.extend(chunk)
        msg_len = msg_len + len(chunk)
    return str(msg_len - 1).rjust(10, '0').encode('utf-8') + b[:-1]
