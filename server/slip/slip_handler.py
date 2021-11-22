from enum import Enum
from typing import Callable
import util.misc as _
from .slip_error import SlipError
import re
import datetime


class MessagePart(Enum):
    HEADER = 'Header'
    BODY = 'Body'


class PeekEvent(Enum):
    collected_header = 'collected-header'
    collected_body = 'collected-body'
    received_chunk = 'received-chunk'

    body_size = 'body-size'
    header_size = 'header-size'


class SlipHandler:
    __curr_message_part = MessagePart.HEADER
    __header_chunk_idx = 0
    __to_collect = 0
    __body = b''
    __is_body_collected = False

    __listeners = {
        PeekEvent.collected_header: [],
        PeekEvent.collected_body: [],
        PeekEvent.received_chunk: [],
        PeekEvent.body_size: [],
        PeekEvent.header_size: []
    }

    def on(self, event: PeekEvent, listener: Callable):
        self.__listeners[event].append(listener)

    def emit(self, event: PeekEvent, *args):
        for listener in self.__listeners[event]:
            listener(*args)

    def __collect_header(self, data: bytes):
        sz = len(data)
        idx = 0
        while idx < sz:
            one_byte = data[idx]
            idx += 1
            meaningful_part = one_byte % 128
            self.__to_collect += meaningful_part * (2 ** self.__header_chunk_idx)
            self.__header_chunk_idx += 7
            if one_byte < 128:  # highest bit is not 1
                self.__curr_message_part = MessagePart.BODY
                self.__body = b''
                self.emit(PeekEvent.collected_header, self.__to_collect)
                break
        if idx != sz:
            self.__collect_body(data[idx:])

    def __collect_body(self, data):
        sz = len(data)
        # for some strange cases when we receive more data then expected
        if sz > self.__to_collect:
            byte_str = _.w_amount(sz - self.__to_collect, 'byte')
            raise SlipError(f'Received {byte_str} bytes more then expected while collecting message body')

        to_append = min(self.__to_collect, sz)
        self.__body += data[:to_append]
        self.__to_collect -= to_append
        if self.__to_collect == 0:
            self.emit(PeekEvent.collected_body)
            self.__curr_message_part = MessagePart.HEADER
            self.__header_chunk_idx = 0
            self.__is_body_collected = True

    __TYPES = {
        'datetime': 'd',
        'str': 'l',
        'number': 'n',
        'dict': 's',
        'file': 'f',
        'bool': 'b',
        'list': 'a',
        'NoneType': 'x',
    }

    __USING_RAW_BYTES = [__TYPES['dict'], __TYPES['list'], __TYPES['file']]

    @staticmethod
    def escape_characters(text: str) -> str:
        return re.sub(r'[;|>]', lambda match_obj: f'>{match_obj.group(0)}', text)

    @staticmethod
    def unescape_characters(text: str) -> str:
        return re.sub(r'>;|>>|>\|', lambda match_obj: match_obj.group(0)[1], text)

    @staticmethod
    def find_border(text: str, from_idx: int, character: str) -> str or None:
        idx = from_idx
        while idx < len(text):
            curr_char = text[idx]
            if curr_char == character:
                return text[from_idx:idx]

            idx += 2 if curr_char == '>' else 1

        return None

    @staticmethod
    def get_date(date_str: str) -> datetime.datetime:
        iso_format = r'^(-\d{6})|(\d{4})-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}\.\d{3}Z$'
        if not re.match(iso_format, date_str):
            raise SlipError('Dates must be in ISO format')

        try:
            date = datetime.datetime.fromisoformat(date_str[:-1])
        except ValueError:
            raise SlipError(f'Could not parse Date from {date_str}')

        return date

    @staticmethod
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

    @classmethod
    def make_body(cls, data: dict or list, files_dict: dict = None) -> bytes:
        result = b''

        if type(data) == dict:
            items = data.items()
        else:
            items = []
            for i in range(len(data)):
                items.append((str(i), data[i]))

        for key, val in items:
            clean_type = _.get_clean_type(val)
            the_type = cls.__TYPES.get(clean_type, None)
            filename = None
            if the_type == cls.__TYPES['NoneType']:
                representation = bytes(0)
            elif the_type == cls.__TYPES['datetime']:
                representation = _.to_b(f'{val.isoformat(timespec="milliseconds")}Z')
            elif the_type == cls.__TYPES['list']:
                representation = cls.make_body(val, files_dict.get(key, None) if files_dict else None)
            elif _.includes(['int', 'float'], clean_type):
                representation = _.to_b(str(val))
                the_type = cls.__TYPES['number']
            elif clean_type == 'bytes':
                is_file = _.includes(files_dict.keys(), key)
                representation = val
                if is_file:
                    filename = files_dict.get(key)
                    the_type = cls.__TYPES['file']
                else:
                    the_type = cls.__TYPES['str']
            elif the_type == cls.__TYPES['dict']:
                representation = cls.make_body(val, files_dict.get(key, None) if files_dict else None)
            else:
                representation = {
                    cls.__TYPES['str']: lambda v: v,
                    cls.__TYPES['bool']: lambda v: str(+v)
                }.get(the_type, None)(val)
                if representation is None:
                    raise SlipError(f"Can't serialize {clean_type} {key}")

                representation = _.to_b(representation)

            esc_key = cls.escape_characters(key)
            if _.includes([cls.__TYPES['bool'], cls.__TYPES['NoneType']], the_type):
                sz = ''
            else:
                sz = f'{len(representation)}|'

            serialized_filename = bytes(cls.escape_characters(filename), 'utf8') if filename else None

            result += _.to_b(f'{esc_key}|{the_type}{sz}')
            result += representation
            if serialized_filename:
                result += serialized_filename
            result += _.to_b(';')

        return result

    @staticmethod
    def parse_bool(text: str) -> bool:
        if not _.includes(['0', '1'], text):
            raise SlipError(f'Boolean must be defined as 0 or 1, got {text}')
        return bool(_.num(text))

    @classmethod
    def parse_list(cls, buf: bytes) -> list:
        as_dict = cls.parse_body(buf)
        as_list = list(as_dict.items())
        as_list.sort()
        return _.l_map(lambda entry: entry[1], as_list)

    @classmethod
    def parse_body(cls, payload: bytes) -> dict:
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
            raw_key = cls.find_border(as_string, current_str_idx, '|')
            if raw_key is None:
                raise SlipError()
            if len(raw_key) == 0:
                raise SlipError(f"Key can't be empty; could only parse until token at position {current_str_idx}")

            # key + |
            change_idx(1, raw_key)

            key = cls.unescape_characters(raw_key)
            the_type = _.invert(cls.__TYPES).get(as_string[current_str_idx], None)
            if the_type is None:
                raise SlipError({'str': as_string, 'idx': current_str_idx})

            type_symbol = cls.__TYPES[the_type]
            # type symbol is one letter
            change_idx(1)

            if type_symbol == cls.__TYPES['bool']:
                content_size = 1
            elif type_symbol == cls.__TYPES['NoneType']:
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
                cls.__TYPES['datetime']: lambda: cls.get_date(raw_content_as_string),
                cls.__TYPES['str']: lambda: raw_content_as_string,
                cls.__TYPES['number']: lambda: _.num(raw_content_as_string),
                cls.__TYPES['dict']: lambda: cls.parse_body(raw_content),
                cls.__TYPES['file']: lambda: {'file': raw_content},
                cls.__TYPES['bool']: lambda: cls.parse_bool(raw_content_as_string),
                cls.__TYPES['list']: lambda: cls.parse_list(raw_content),
                cls.__TYPES['NoneType']: lambda: None
            }[type_symbol]()

            if not _.includes(cls.__USING_RAW_BYTES, type_symbol):
                change_idx(0, raw_content_as_string)
            else:
                current_str_idx += len(raw_content_as_string)
                current_buf_idx += content_size
                check_len()

            # has file name
            if as_string[current_str_idx] != ';':
                if type_symbol != cls.__TYPES['file']:
                    raise SlipError(f'{key} is defined as {the_type}, but has a file name')
                raw_file_name = cls.find_border(as_string, current_str_idx, ';')
                if raw_file_name is None:
                    raise SlipError(f'Failed while extracting the file name for {key}')
                result[key]['name'] = cls.unescape_characters(raw_file_name)
                # filename
                change_idx(0, raw_file_name)
            elif type_symbol == cls.__TYPES['file']:
                raise SlipError(f'{key} is defined as a File, but has no file name')

            change_idx(1)

        return result

    def make_message(self, data, files_dict):
        body = self.make_body(data, files_dict)
        self.emit(PeekEvent.body_size, len(body))
        header = self.make_header(body)
        self.emit(PeekEvent.header_size, len(header))
        return header + body

    def feed(self, data_chunk):
        self.emit(PeekEvent.received_chunk, len(data_chunk), self.__curr_message_part)
        self.__is_body_collected = False
        if self.__curr_message_part == MessagePart.HEADER:
            self.__collect_header(data_chunk)
        else:
            self.__collect_body(data_chunk)

        return self.parse_body(self.__body) if self.__is_body_collected else None
