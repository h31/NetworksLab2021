OPCODES = {
    'unknown': 0,
    'rrq': 1,
    'wrq': 2,
    'data': 3,
    'ack': 4,
    'error': 5
}

ERROR_CODE = {
    0: "Not defined, see error message (if any).",
    1: "File not found.",
    2: "Access violation.",
    3: "Disk full or allocation exceeded.",
    4: "Illegal TFTP operation.",
    5: "Unknown transfer ID.",
    6: "File already exists.",
    7: "No such user."
}

MODE = {
    'netascii': 0,
    'octet': 1,
    'mail': 2
}
