import binascii

RRTYPE = {1: 'A', 15: 'MX', 16: 'TXT', 28: 'AAAA'}
RCODE = ['', 'Format error!', 'Server failure!', 'Name Error!', 'Not Implemented!', 'Refused!']


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
        seclen = int(self.hex_message[hn:hn + 2], 16)
        domain_name = []
        while seclen:
            if self.bit_message[bn:bn + 2] == '11':
                domain_name.extend(
                    self.resolve_compression(int(self.bit_message[bn + 2:bn + 16], 2) * 2,
                                             int(self.bit_message[bn + 2:bn + 16], 2) * 8))
                break
            hn += 2
            bn += 8
            section = ''
            for _ in range(seclen):
                section += bytearray.fromhex(self.hex_message[hn:hn + 2]).decode()
                hn += 2
                bn += 8
            seclen = int(self.hex_message[hn:hn + 2], 16)
            domain_name.append(section)
        return domain_name

    def resolve(self):
        seclen = int(self.get_field(1, 0, 2), 16)
        domain_name = []
        while seclen:
            if self.get_field(0, 0, 2) == '11':
                domain_name.extend(self.resolve_compression(int(self.get_field(0, 2, 16), 2) * 2,
                                                            int(self.get_field(0, 2, 16), 2) * 8))
                self.shift_pointer(2)
                break
            self.shift_pointer(2)
            section = ''
            for _ in range(seclen):
                section += bytearray.fromhex(self.get_field(1, 0, 2)).decode()
                self.shift_pointer(2)
            seclen = int(self.get_field(1, 0, 2), 16)
            domain_name.append(section)
        self.shift_pointer(2)
        return '.'.join(domain_name)

    def get_ans(self, qtype):
        ans = {'NAME': self.get_field(0, 0, 16)}
        self.shift_pointer(4)
        ans['TYPE'] = self.get_field(0, 0, 16)
        self.shift_pointer(4)
        ans['CLASS'] = self.get_field(0, 0, 16)
        self.shift_pointer(4)
        ans['TTL'] = self.get_field(0, 0, 32)
        self.shift_pointer(8)
        ans['RDLENGTH'] = int(self.get_field(0, 0, 16), 2)
        self.shift_pointer(4)
        if qtype == 'A':
            ip4 = []
            for _ in range(4):
                ip4.append(str(int(self.get_field(1, 0, 2), 16)))
                self.shift_pointer(2)
            ans['RDATA'] = '.'.join(ip4)
        elif qtype == 'MX':
            ans['PREFERENCE'] = self.get_field(0, 0, 16)
            self.shift_pointer(4)
            ans['RDATA'] = self.resolve()
        elif qtype == 'TXT':
            txt = []
            for _ in range(ans['RDLENGTH']):
                txt.append(bytearray.fromhex(self.get_field(1, 0, 2)).decode())
                self.shift_pointer(2)
            ans['RDATA'] = ''.join(txt[1:])
        elif qtype == 'AAAA':
            ip6 = []
            for _ in range(8):
                ip6.append(self.get_field(1, 0, 2))
                self.shift_pointer(2)
            ans['RDATA'] = ':'.join(ip6)
        return ans


def parse_response(message):
    """
    Парсер сообщений от DNS-сервера
    :param message: сообщение от сервера
    :type message: bytes
    """
    message = Message(message)
    res = {'ID': message.hex_message[0:4], 'QR': message.bit_message[16], 'Opcode': message.bit_message[17:21],
           'AA': message.bit_message[21], 'TC': message.bit_message[22], 'RD': message.bit_message[23],
           'RA': message.bit_message[24],
           'Z': message.bit_message[25:28], 'RCODE': RCODE[int(message.bit_message[28:32], 2)],
           'QDCOUNT': message.bit_message[32:48],
           'ANCOUNT': int(message.bit_message[48:64], 2), 'NSCOUNT': message.bit_message[64:80],
           'ARCOUNT': message.bit_message[80:96]}

    message.shift_pointer(24)
    res['QNAME'] = message.resolve()
    res['QTYPE'] = RRTYPE[int(message.get_field(0, 0, 16), 2)]

    message.shift_pointer(4)
    res['QCLASS'] = message.get_field(0, 0, 16)
    message.shift_pointer(4)
    res['ANS'] = []
    for _ in range(res['ANCOUNT']):
        res['ANS'].append(message.get_ans(res['QTYPE']))
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
    message += "{0:016b}".format((list(RRTYPE.keys())[list(RRTYPE.values()).index(RRType)]))  # QTYPE

    message += '0000000000000001'  # QCLASS - Internet
    return str(hex(int(message, 2)))[2:]
