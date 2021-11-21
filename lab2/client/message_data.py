import binascii


class Message:
    def __init__(self, raw):
        self.hex_pointer = 0
        self.bit_pointer = 0
        self.hex_message = binascii.hexlify(raw).decode("utf-8")
        self.bit_message = bin(int(self.hex_message, 16))[2:].zfill(len(self.hex_message) * 4)

    def get_field(self, hex_flag, offset, field_length):
        if hex_flag:
            return self.hex_message[self.hex_pointer + offset:self.hex_pointer + field_length]
        else:
            return self.bit_message[self.bit_pointer + offset:self.bit_pointer + field_length]

    def shift_pointer(self, n):
        """
        Смещение указателей сообщения
        :param n: величина смещения шестнадцатеричного указателя
        """
        self.hex_pointer += n
        self.bit_pointer += 4 * n

    def resolve_compression(self, hn, bn):
        """
        Разрешение доменного имени при использовании сокращения
        :param hn: смещение в шестнадцатеричном формате
        :param bn: смещение в двоичном формате
        """
        qname = True
        domain_name = []
        while qname:
            if self.bit_message[bn:bn + 2] == '11':
                domain_name.extend(
                    self.resolve_compression(int(self.bit_message[bn + 2:bn + 16], 2) * 2,
                                             int(self.bit_message[bn + 2:bn + 16], 2) * 8))
                break
            seclen = int(self.hex_message[hn:hn + 2], 16)
            hn += 2
            bn += 8
            section = ''
            for _ in range(seclen):
                section += bytearray.fromhex(self.hex_message[hn:hn + 2]).decode()
                hn += 2
                bn += 8
            qname = int(self.hex_message[hn:hn + 2], 16)
            domain_name.append(section)
        return domain_name


def parse_response(message):
    """
    Парсер сообщений от DNS-сервера
    :param message: сообщение от сервера
    :type message: bytes
    """
    message = Message(message)
    res = {'ID': message.hex_message[0:4], 'QR': message.bit_message[16], 'Opcode': message.bit_message[17:21],
           'AA': message.bit_message[21],
           'TC': message.bit_message[22], 'RD': message.bit_message[23], 'RA': message.bit_message[24],
           'Z': message.bit_message[25:28],
           'RCODE': message.bit_message[28:32], 'QDCOUNT': message.bit_message[32:48],
           'ANCOUNT': int(message.bit_message[48:64], 2),
           'NSCOUNT': message.bit_message[64:80], 'ARCOUNT': message.bit_message[80:96]}

    if res['RCODE'] == '0000':
        res['RCODE'] = ''
    elif res['RCODE'] == '0001':
        res['RCODE'] = 'Format error!'
    elif res['RCODE'] == '0010':
        res['RCODE'] = 'Server failure!'
    elif res['RCODE'] == '0011':
        res['RCODE'] = 'Name Error!'
    elif res['RCODE'] == '0100':
        res['RCODE'] = 'Not Implemented!'
    elif res['RCODE'] == '0101':
        res['RCODE'] = 'Refused!'

    qname = True
    message.shift_pointer(24)
    domain_name = []
    while qname:
        seclen = int(message.get_field(1, 0, 2), 16)
        message.shift_pointer(2)
        section = ''
        for _ in range(seclen):
            section += bytearray.fromhex(message.get_field(1, 0, 2)).decode()
            message.shift_pointer(2)
        qname = int(message.get_field(1, 0, 2), 16)
        domain_name.append(section)
    message.shift_pointer(2)
    res['QNAME'] = '.'.join(domain_name)
    res['QTYPE'] = message.get_field(0, 0, 16)

    if res['QTYPE'] == '0000000000000001':
        res['QTYPE'] = 'A'
    elif res['QTYPE'] == '0000000000001111':
        res['QTYPE'] = 'MX'
    elif res['QTYPE'] == '0000000000010000':
        res['QTYPE'] = 'TXT'
    elif res['QTYPE'] == '0000000000011100':
        res['QTYPE'] = 'AAAA'

    message.shift_pointer(4)
    res['QCLASS'] = message.get_field(0, 0, 16)
    message.shift_pointer(4)
    res['ANS'] = []
    for _ in range(int(res['ANCOUNT'], 2)):
        ans = {'NAME': message.get_field(0, 0, 16)}
        message.shift_pointer(4)
        ans['TYPE'] = message.get_field(0, 0, 16)
        message.shift_pointer(4)
        ans['CLASS'] = message.get_field(0, 0, 16)
        message.shift_pointer(4)
        ans['TTL'] = message.get_field(0, 0, 32)
        message.shift_pointer(8)
        ans['RDLENGTH'] = message.get_field(0, 0, 16)
        message.shift_pointer(4)
        if res['QTYPE'] == 'A':
            ip4 = []
            for _ in range(4):
                ip4.append(str(int(message.get_field(1, 0, 2), 16)))
                message.shift_pointer(2)
            ans['RDATA'] = '.'.join(ip4)
            res['ANS'].append(ans)
        elif res['QTYPE'] == 'MX':
            ans['PREFERENCE'] = message.get_field(0, 0, 16)
            message.shift_pointer(4)
            qname = True
            mail = []
            while qname:
                if message.get_field(0, 0, 2) == '11':
                    mail.extend(message.resolve_compression(int(message.get_field(0, 2, 16), 2) * 2,
                                                            int(message.get_field(0, 2, 16), 2) * 8))
                    message.shift_pointer(2)
                    break
                seclen = int(message.get_field(1, 0, 2), 16)
                message.shift_pointer(2)
                section = ''
                for _ in range(seclen):
                    section += bytearray.fromhex(message.get_field(1, 0, 2)).decode()
                    message.shift_pointer(2)
                qname = int(message.get_field(1, 0, 2), 16)
                mail.append(section)
            message.shift_pointer(2)
            ans['RDATA'] = '.'.join(mail)
            res['ANS'].append(ans)
        elif res['QTYPE'] == 'TXT':
            txt = []
            for _ in range(int(ans['RDLENGTH'], 2)):
                txt.append(bytearray.fromhex(message.get_field(1, 0, 2)).decode())
                message.shift_pointer(2)
            ans['RDATA'] = ''.join(txt[1:])
            res['ANS'].append(ans)
        elif res['QTYPE'] == 'AAAA':
            ip6 = []
            for _ in range(8):
                ip6.append(message.get_field(1, 0, 2))
                message.shift_pointer(8)
            ans['RDATA'] = ':'.join(ip6)
            res['ANS'].append(ans)
    return res


def build_request(request_id, RRType, domain_name):
    message = str(request_id)  # ID - 16 бит идентификатор запроса
    message += '0'  # QR - запрос
    message += '0000'  # Opcode - запрос
    message += '0'  # AA
    message += '0'  # TC - короткое сообщение
    message += '1'  # RD - желательна рекурсия
    message += '0'  # RA
    message += '000'  # Z
    message += '0000'  # RCODE
    message += '0000000000000001'  # QDCOUNT - 1 вопрос
    message += '0000000000000000'  # ANCOUNT - 0 ответов
    message += '0000000000000000'  # NSCOUNT - 0 записей об уполномоченных серверах
    message += '0000000000000000'  # ARCOUNT - 0 дополнительных записей

    for domain_name_section in domain_name.split('.'):
        message += "{0:08b}".format(
            len(domain_name_section))  # Байт беззнакового целого, обозначающий количество байт в секции
        for ch in domain_name_section:
            message += "{0:08b}".format(ord(ch))  # ASCII-код символа в секции доменного имени
    message += '00000000'  # Завершение секции QNAME

    if RRType == 'A':
        message += '0000000000000001'  # QTYPE - A
    elif RRType == 'MX':
        message += '0000000000001111'  # QTYPE - MX
    elif RRType == 'TXT':
        message += '0000000000010000'  # QTYPE - TXT
    elif RRType == 'AAAA':
        message += '0000000000011100'  # QTYPE - AAAA

    message += '0000000000000001'  # QCLASS - Internet
    return str(hex(int(message, 2)))[2:]
