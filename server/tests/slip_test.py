import unittest
from slip import SlipHandler, SlipError
from util.misc import *
from os import path, makedirs
from datetime import datetime
from copy import deepcopy
from random import randint

original_str = 'key|with|sticks>and>>arrows>and se;micolons'
original_data = {
  '>strange->key': 12.43,
  'data': {'idx': 12, 'val': None, 'when': now()},
  # To ensure strange encodings don't mess everything up
  'message': 'Привет! Ô',
  'is_read': False,
  '|shed|': '|-|-|-|-|-|-|',
  'yaharr': [1, 2, {'obj': 'with-one-field'}]
}


class SlipTestCase(unittest.TestCase):
    def test_escape_characters(self):
        self.assertEqual(
            'key>|with>|sticks>>and>>>>arrows>>and se>;micolons',
            SlipHandler.escape_characters(original_str)
        )

    def test_unescape_characters(self):
        escaped = SlipHandler.escape_characters(original_str)
        unescaped = SlipHandler.unescape_characters(escaped)
        self.assertEqual(unescaped, original_str)

    def test_find_border(self):
        original_key = '|key-made-by-s>>ome-weirdo'
        info_after_key = '||>|gone wild with these!|||>||;<>|'
        serialized_data = f'{SlipHandler.escape_characters(original_key)}|{info_after_key}'
        found_key = SlipHandler.find_border(serialized_data, 0, '|')
        parsed_key = SlipHandler.unescape_characters(found_key)
        self.assertEqual(parsed_key, original_key)

    @staticmethod
    def serialization_fixture(payload, files=None):
        slip_handler = SlipHandler()
        serialized_data = slip_handler.make_message(payload, files)
        return slip_handler, serialized_data

    @staticmethod
    def imitate_chunks(byte_data: bytes, max_sz: int = 1024):
        chunks = []
        sz = len(byte_data)
        offset = 0
        while sz - offset > 0:
            chunk_size = min(randint(1, max_sz), sz - offset)
            chunk = byte_data[offset:offset + chunk_size]
            chunks.append(chunk)
            offset += chunk_size
        return chunks

    def use_chunks_fixture(self, slip_handler: SlipHandler, byte_data: bytes, max_sz: int = 1024):
        chunks = self.imitate_chunks(byte_data, max_sz)
        deserialized_data = None
        for chunk in chunks:
            deserialized_data = slip_handler.feed(chunk)
        return deserialized_data

    def test_serialize(self):
        payload = {
            'str': 'A String',
            'num': 12,
            'none': None,
            'emptyObj': {},
            'encodedStr': 'Привет! Ô'
        }
        _, serialized_data = self.serialization_fixture(payload)
        self.assertEqual(
            # N is the Header: one byte containing number 78
            'Nstr|l8|A String;num|n2|12;none|x;emptyObj|s0|;encodedStr|l16|Привет! Ô;',
            str(serialized_data, 'utf8')
        )

    def test_deserialize(self):
        slip_handler, serialized_data = self.serialization_fixture(original_data)
        deserialized_data = self.use_chunks_fixture(slip_handler, serialized_data)
        self.assertEqual(original_data, deserialized_data)

    def test_deserialize_invalid(self):
        with self.assertRaises(SlipError):
            empty_key = 'first_key|l4|wooo;|b1'
            SlipHandler.parse_body(bytes(empty_key, 'utf8'))

        with self.assertRaises(SlipError):
            invalid_bool = 'is_good|b1;is_bad|b8;'
            SlipHandler.parse_body(bytes(invalid_bool, 'utf8'))

        with self.assertRaises(SlipError):
            file_no_filename = 'str|l5|Hello;num|n1|5the-file-no-one-needs.txt;'
            SlipHandler.parse_body(bytes(file_no_filename, 'utf8'))

        with self.assertRaises(SlipError):
            non_iso_date = 'who|l2|me;dob|d10|2000-07-17;'
            SlipHandler.parse_body(bytes(non_iso_date, 'utf8'))

        with self.assertRaises(SlipError):
            invalid_date = 'suggestedBy|l12|Münchhausen;whatSuggested|d24|1979-05-32T12:00:00.000Z'
            SlipHandler.parse_body(bytes(invalid_date, 'utf8'))

    def attachment_fixture(
            self,
            original_name: str,
            payload: dict,
            route: str or list,
            old_handler: SlipHandler = None
    ):
        f_path = path.join(dirname(__file__), 'original_files', original_name)
        with open(f_path, 'rb') as file:
            f_bytes = file.read()

        to_serialize = deepcopy(payload)
        files_dict = {}
        set_v(to_serialize, route, f_bytes)
        set_v(files_dict, route, original_name)

        new_handler, serialized_data = self.serialization_fixture(to_serialize, files_dict)
        handler = old_handler if old_handler else new_handler
        deserialized_data = self.use_chunks_fixture(handler, serialized_data, 2048)

        to_compare = deepcopy(payload)
        set_v(to_compare, route, {'file': f_bytes, 'name': original_name})

        self.assertEqual(to_compare, deserialized_data)

        parsed_dir = path.join(dirname(__file__), 'parsed_files')
        if not path.exists(parsed_dir):
            makedirs(parsed_dir)

        parsed_file_data = get_v(deserialized_data, route)
        path_to_parsed_file = path.join(parsed_dir, parsed_file_data['name'])

        with open(path_to_parsed_file, 'wb+') as parsed_img_file:
            parsed_img_file.write(parsed_file_data['file'])

    def test_attachments(self):
        handler = SlipHandler()
        img_payload = {
            'user': 'Barash',
            'message': 'Беее...',
            'profile_data': {'created': datetime(2002, 11, 20)}
        }
        self.attachment_fixture('good.jpg', img_payload, 'profile_data.avatar', handler)

        doc_payload = {
            'band': 'Smash Mouth',
            'date': datetime(year=2005, month=8, day=23),
            'time': '3.20'
        }
        self.attachment_fixture('doc.txt', doc_payload, 'text', handler)

    def test_make_header(self):
        data = bytes(map(lambda x: x % 256, range(30000)))
        self.assertEqual('b0 ea 01', SlipHandler.make_header(data).hex(' '))


if __name__ == '__main__':
    unittest.main()
