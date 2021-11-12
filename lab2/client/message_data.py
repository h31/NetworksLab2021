import binascii


def parse_response(message):
    """
    Парсер сообщений от DNS-сервера
    :param message: сообщение от сервера
    :type message: bytes
    """
    hex_message = binascii.hexlify(message).decode("utf-8")
    bin_message = bin(int(hex_message, 16))[2:].zfill(len(hex_message) * 4)
    res = {'ID': hex_message[0:4], 'QR': bin_message[16], 'Opcode': bin_message[17:21], 'AA': bin_message[21],
           'TC': bin_message[22], 'RD': bin_message[23], 'RA': bin_message[24], 'Z': bin_message[25:28],
           'RCODE': bin_message[28:32], 'QDCOUNT': bin_message[32:48], 'ANCOUNT': bin_message[48:64],
           'NSCOUNT': bin_message[64:80], 'ARCOUNT': bin_message[80:96]}

    qname = True
    bn = 96
    hn = 24
    url = []
    while qname:
        seclen = int(hex_message[hn:hn + 2], 16)
        hn += 2
        bn += 8
        section = ''
        for _ in range(seclen):
            section += bytearray.fromhex(hex_message[hn:hn + 2]).decode()
            hn += 2
            bn += 8
        qname = int(hex_message[hn:hn + 2], 16)
        url.append(section)
    hn += 2
    bn += 8
    res['QNAME'] = '.'.join(url)
    res['QTYPE'] = bin_message[bn:bn + 16]

    rr_type = ''
    if res['QTYPE'] == '0000000000000001':
        rr_type = 'A'
    elif res['QTYPE'] == '0000000000001111':
        rr_type = 'MX'
    elif res['QTYPE'] == '0000000000010000':
        rr_type = 'TXT'
    elif res['QTYPE'] == '0000000000011100':
        rr_type = 'AAAA'
    res['rrtype'] = rr_type

    hn += 4
    bn += 16
    res['QCLASS'] = bin_message[bn:bn + 16]
    hn += 4
    bn += 16
    res['ANS'] = []
    for _ in range(int(res['ANCOUNT'], 2)):
        ans = {'NAME': bin_message[bn:bn + 16]}
        hn += 4
        bn += 16
        ans['TYPE'] = bin_message[bn:bn + 16]
        hn += 4
        bn += 16
        ans['CLASS'] = bin_message[bn:bn + 16]
        hn += 4
        bn += 16
        ans['TTL'] = bin_message[bn:bn + 32]
        hn += 8
        bn += 32
        ans['RDLENGTH'] = bin_message[bn:bn + 16]
        hn += 4
        bn += 16
        if rr_type == 'A':
            ip4 = []
            for _ in range(int(ans['RDLENGTH'], 2)):  # 4
                ip4.append(str(int(hex_message[hn:hn + 2], 16)))
                hn += 2
                bn += 8
            ans['RDATA'] = '.'.join(ip4)
            res['ANS'].append(ans)
        elif rr_type == 'MX':
            ans['PREFERENCE'] = bin_message[bn:bn + 16]
            hn += 4
            bn += 16
            qname = True
            mail = []
            while qname:
                if bin_message[bn:bn + 16] == ans['NAME']:
                    mail.extend(url)
                    break
                seclen = int(hex_message[hn:hn + 2], 16)
                hn += 2
                bn += 8
                section = ''
                for _ in range(seclen):
                    section += bytearray.fromhex(hex_message[hn:hn + 2]).decode()
                    hn += 2
                    bn += 8
                qname = int(hex_message[hn:hn + 2], 16)
                mail.append(section)
            hn += 2
            bn += 8
            ans['RDATA'] = '.'.join(mail)
            res['ANS'].append(ans)
        elif rr_type == 'TXT':
            txt = []
            for _ in range(int(ans['RDLENGTH'], 2)):
                txt.append(bytearray.fromhex(hex_message[hn:hn + 2]).decode())
                hn += 2
                bn += 8
            ans['RDATA'] = ''.join(txt[1:])
            res['ANS'].append(ans)
        elif rr_type == 'AAAA':
            ip6 = []
            for _ in range(int(ans['RDLENGTH'], 2)):  # 6
                ip6.append(hex_message[hn:hn + 2])
                hn += 2
                bn += 8
            ans['RDATA'] = ':'.join(ip6)
            res['ANS'].append(ans)
    return res


def build_request(request_id, RRType, URL):
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

    for URL_section in URL.split('.'):
        message += "{0:08b}".format(len(URL_section))  # Байт беззнакового целого, обозначающий количество байт в секции
        for ch in URL_section:
            message += "{0:08b}".format(ord(ch))  # ASCII-код символа в секции URL
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
