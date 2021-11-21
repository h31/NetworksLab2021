import re
import datetime
from .slip_error import SlipError
import util.misc as _


TYPES = {
    'datetime': 'd',
    'str': 'l',
    'number': 'n',
    'dict': 's',
    'file': 'f',
    'bool': 'b',
    'list': 'a',
    'NoneType': 'x',
}

USING_RAW_BYTES = [TYPES['dict'], TYPES['list'], TYPES['file']]


def escape_characters(text: str) -> str:
    return re.sub(r'[;|>]', lambda match_obj: f'>{match_obj.group(0)}', text)


def unescape_characters(text: str) -> str:
    return re.sub(r'>;|>>|>\|', lambda match_obj: match_obj.group(0)[1], text)


def find_border(text: str, from_idx: int, character: str) -> str or None:
    idx = from_idx
    while idx < len(text):
        curr_char = text[idx]
        if curr_char == character:
            return text[from_idx:idx]

        idx += 2 if curr_char == '>' else 1

    return None


def get_date(date_str: str) -> datetime.datetime:
    iso_format = r'^(-\d{6})|(\d{4})-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$'
    if not re.match(iso_format, date_str):
        raise SlipError('Dates must be in ISO format')

    try:
        date = datetime.datetime.fromisoformat(date_str[:-1])
    except ValueError:
        raise SlipError(f'Could not parse Date from {date_str}')

    return date


def make_header(data: bytes) -> bytes:
    size = '{0:b}'.format(len(data))
    header_bytes = []
    left = len(size)
    while left > 0:
        first_bit = '0' if len(size) < 8 else '1'
        part = int(f'{first_bit}{size[-7:]}', 2)
        header_bytes.append(part)
        size = size[:-7]
        left -= 7
    return bytes(header_bytes)


def serialize(data: dict or list, files_dict: dict = None) -> bytes:
    result = b''

    if type(data) == dict:
        items = data.items()
    else:
        items = []
        for i in range(len(data)):
            items.append((str(i), data[i]))

    for key, val in items:
        clean_type = _.get_clean_type(val)
        the_type = TYPES.get(clean_type, None)
        filename = None
        if the_type == TYPES['NoneType']:
            representation = bytes(0)
        elif the_type == TYPES['datetime']:
            representation = _.to_b(f'{val.isoformat(timespec="milliseconds")}Z')
        elif the_type == TYPES['list']:
            representation = serialize(val, files_dict.get(key, None) if files_dict else None)
        elif _.includes(['int', 'float'], clean_type):
            representation = _.to_b(str(val))
            the_type = TYPES['number']
        elif clean_type == 'bytes':
            is_file = _.includes(files_dict.keys(), key)
            representation = val
            if is_file:
                filename = files_dict.get(key)
                the_type = TYPES['file']
            else:
                the_type = TYPES['str']
        elif the_type == TYPES['dict']:
            representation = serialize(val, files_dict.get(key, None) if files_dict else None)
        else:
            representation = {
                TYPES['str']: lambda v: v,
                TYPES['bool']: lambda v: str(+v)
            }.get(the_type, None)(val)
            if representation is None:
                raise SlipError(f"Can't serialize {clean_type} {key}")

            representation = _.to_b(representation)

        esc_key = escape_characters(key)
        if _.includes([TYPES['bool'], TYPES['NoneType']], the_type):
            sz = ''
        else:
            sz = f'{len(representation)}|'

        serialized_filename = bytes(escape_characters(filename), 'utf8') if filename else None

        result += _.to_b(f'{esc_key}|{the_type}{sz}')
        result += representation
        if serialized_filename:
            result += serialized_filename
        result += _.to_b(';')

    return result


def parse_bool(text: str) -> bool:
    if not _.includes(['0', '1'], text):
        raise SlipError(f'Boolean must be defined as 0 or 1, got {text}')
    return bool(_.num(text))


def parse_list(buf: bytes) -> list:
    as_dict = deserialize(buf)
    as_list = list(as_dict.items())
    as_list.sort()
    return _.l_map(lambda entry: entry[1], as_list)


def deserialize(payload: bytes) -> dict:
    clean_type = _.get_clean_type(payload)
    if clean_type != 'bytes':
        raise SlipError(f'Expected a byte array, got {clean_type}')

    as_string = _.from_b(payload)
    result = {}
    current_buf_idx = 0
    current_str_idx = 0

    def check_len():
        nonlocal current_buf_idx
        if current_buf_idx > len(payload):
            raise SlipError()

    def change_idx(change: int, text: str = None):
        nonlocal current_str_idx
        nonlocal current_buf_idx

        current_str_idx += change
        current_buf_idx += change

        if text:
            current_str_idx += len(text)
            current_buf_idx += len(_.to_b(text))

        check_len()

    while current_buf_idx < len(payload):
        raw_key = find_border(as_string, current_str_idx, '|')
        if raw_key is None:
            raise SlipError()
        if len(raw_key) == 0:
            raise SlipError(f"Key can't be empty; could only parse until token at position {current_str_idx}")

        # key + |
        change_idx(1, raw_key)

        key = unescape_characters(raw_key)
        the_type = _.invert(TYPES).get(as_string[current_str_idx], None)
        if the_type is None:
            raise SlipError({'str': as_string, 'idx': current_str_idx})

        type_symbol = TYPES[the_type]
        # type symbol is one letter
        change_idx(1)

        if type_symbol == TYPES['bool']:
            content_size = 1
        elif type_symbol == TYPES['NoneType']:
            content_size = 0
        else:
            try:
                content_size_border = as_string.index('|', current_str_idx)
            except ValueError:  # no border found at all
                raise SlipError()

            if content_size_border == 0:  # no content size provided
                raise SlipError(f'Content size is required for type {the_type}')

            content_size_str = as_string[current_str_idx:content_size_border]
            try:
                content_size = int(content_size_str)
            except ValueError:
                raise SlipError('Content size must be an integer')

            # size + |
            change_idx(1, content_size_str)

        raw_content = payload[current_buf_idx:current_buf_idx + content_size]
        raw_content_as_string = _.from_b(raw_content)

        result[key] = {
            TYPES['datetime']: lambda: get_date(raw_content_as_string),
            TYPES['str']: lambda: raw_content_as_string,
            TYPES['number']: lambda: _.num(raw_content_as_string),
            TYPES['dict']: lambda: deserialize(raw_content),
            TYPES['file']: lambda: {'file': raw_content},
            TYPES['bool']: lambda: parse_bool(raw_content_as_string),
            TYPES['list']: lambda: parse_list(raw_content),
            TYPES['NoneType']: lambda: None
        }[type_symbol]()

        if not _.includes(USING_RAW_BYTES, type_symbol):
            change_idx(0, raw_content_as_string)
        else:
            current_str_idx += len(raw_content_as_string)
            current_buf_idx += content_size
            check_len()

        # has file name
        if as_string[current_str_idx] != ';':
            if type_symbol != TYPES['file']:
                raise SlipError(f'{key} is defined as {the_type}, but has a file name')
            raw_file_name = find_border(as_string, current_str_idx, ';')
            if raw_file_name is None:
                raise SlipError(f'Failed while extracting the file name for {key}')
            result[key]['name'] = unescape_characters(raw_file_name)
            # filename
            change_idx(0, raw_file_name)
        elif type_symbol == TYPES['file']:
            raise SlipError(f'{key} is defined as a File, but has no file name')

        change_idx(1)

    return result
