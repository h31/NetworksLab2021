TFTP_OPCODES = {
    'read': 1,
    'write': 2,
    'data': 3,
    'ack': 4,
    'error': 5
}

TFTP_SERVER_ERRORS = {
    1: "File not found.",
    2: "Access violation.",
    3: "Disk full or allocation exceeded.",
    4: "Illegal TFTP operation.",
    5: "Unknown transfer ID.",
    6: "File already exists.",
    7: "No such user."
}
