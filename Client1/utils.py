# Коды операций
RRQ = b'\x00\x01' # запрос на чтение
WRQ = b'\x00\x02' # запрос на запись
DATA = b'\x00\x03' # пакет с файлом
ACK = b'\x00\x04' # пакет подтверждения
ERROR = b'\x00\x05' # пакет с ошибкой
UNKNOWN = b'\x00\x00' # неизвестная операция

# Коды ошибок
FILE_NOT_FOUND = b'\x00\x01'
ACCESS_VIOLATION = b'\x00\x02'
DISK_FULL = b'\x00\x03'
ILLEGAL_OPERATION = b'\x00\x04'
UNKNOWN_TRANFER_ID = b'\x00\x05'
FILE_EXIST = b'\x00\x06'
NO_SUCH_USER = b'\x00\x07'

# Текстовое значение сообщений об ошибке
ERR_MESSAGE = {
    UNKNOWN: "Error not defined",
    FILE_NOT_FOUND: "File not found",
    ACCESS_VIOLATION: "Access violation",
    DISK_FULL: "Disk full or allocation exceeded",
    ILLEGAL_OPERATION: "Illegal TFTP operation",
    FILE_EXIST: "File already exists",
    NO_SUCH_USER: "No such user"
}

# Функция чтобы создать дата пакет
def getDataPacket(blockNumber, data):
    bytesNumber = blockNumber.to_bytes(2, 'big')
    bytesData = DATA + bytesNumber + data
    return bytesData

# Функция чтобы создать пакет подтверждения
def getAcknowledgePacket(blockNumber):
    bytesNumber = blockNumber.to_bytes(2, 'big')
    bytesAcknowledge = ACK + bytesNumber
    return bytesAcknowledge

# Функция чтобы создать пакет с ошибкой
def getErrorPacket(type):
    return ERROR + type + ERR_MESSAGE[type].encode('utf-8') + b'\x00'

# Функция чтобы узнать код операции пакета
def getOpCode(packet):
    return packet[0:2]

# Функция чтобы узнать номер блока пакета
def getBlockNumber(packet):
    number = int.from_bytes(packet[2:4], 'big')
    return number

# Функция чтобы получить дата из пакета
def getData(packet):
    return packet[4:]

# Функция чтобы получить код ошибки из пакета
def getErrorCode(packet):
    return int.from_bytes(packet[2:4], 'big')

# Функция чтобы получить сообщение ошибки из пакета
def getErrorMessage(packet):
    return packet[4:-1].decode('utf-8')

# Функция чтобы получить имя файла из пакета
def getFileName(packet):
    return packet[2:-1].partition(b'\x00')[0].decode('utf-8')

# Константные значения
PACKET_SIZE = 512 # размер пакета данных
MAX_BLOCK_SIZE = 1024 # максимально возможный размер пакета