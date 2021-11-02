def parse_message(message):
    """
    Парсер сообщений от DNS-сервера
    :param message: сообщение от сервера
    :type message: str
    """
    # TODO: написать парсер сообщений от сервера
    return format_hex(message)


def format_hex(hexa):
    """Метод возвращает "человекочитаемую" версию ответа от DNS-сервера"""
    octets = [hexa[i:i + 2] for i in range(0, len(hexa), 2)]
    pairs = [" ".join(octets[i:i + 2]) for i in range(0, len(octets), 2)]
    return "\n".join(pairs)


def build_request(id, RRType, URL):
    message = str(id)  # ID - 16 бит идентификатор запроса
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

    if RRType in ['A', 'AAAA']:
        message += '0000000000000001'  # QTYPE - A, AAAA
    elif RRType == 'MX':
        message += '0000000000001111'  # QTYPE - MX
    elif RRType == 'TXT':
        message += '0000000000010000'  # QTYPE - TXT

    message += '0000000000000001'  # QCLASS - Internet
    return str(hex(int(message, 2)))[2:]
