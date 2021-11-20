import enum


class MessageId(enum.Enum):
    error_message = '0'
    request_connection = '1'
    accept_connection = '2'
    send_simple_message = '3'
    receive_simple_message = '4'
    send_file_message = '5'
    receive_file_message = '6'
    request_close_client = '7'
    receive_close_client = '8'
    request_close_server = '9'

    def __init__(self, msg_id):
        self.msgId = int(msg_id)

    def getId(self):
        return str(self.msgId)

    def fromClient(self):
        if self.msgId % 2 == 1:
            return True
        else:
            return False

    def withFile(self):
        if self.msgId in [5, 6]:
            return True
        else:
            return False

    def isNextFile(self, cur_len):
        if (self.msgId == 5 and cur_len == 3) or (self.msgId == 6 and cur_len == 5):
            return True
        else:
            return False
