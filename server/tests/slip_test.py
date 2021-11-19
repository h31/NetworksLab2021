import unittest
import slip
import util.misc as _
from os import path, makedirs
from datetime import datetime
from copy import deepcopy

original_str = 'key|with|sticks>and>>arrows>and se;micolons'
original_data = {
  '>strange->key': 12.43,
  'data': {'idx': 12, 'val': None, 'when': _.now()},
  # To ensure strange encodings don't mess everything up
  'message': 'Привет! Ô',
  'is_read': False,
  '|shed|': '|-|-|-|-|-|-|',
  'yaharr': [1, 2, {'obj': 'with-one-field'}]
}


class MyTestCase(unittest.TestCase):
    def test_escape_characters(self):
        self.assertEqual(
            'key>|with>|sticks>>and>>>>arrows>>and se>;micolons',
            slip.escape_characters(original_str)
        )

    def test_unescape_characters(self):
        escaped = slip.escape_characters(original_str)
        unescaped = slip.unescape_characters(escaped)
        self.assertEqual(unescaped, original_str)

    def test_find_border(self):
        original_key = '|key-made-by-s>>ome-weirdo'
        info_after_key = '||>|gone wild with these!|||>||;<>|'
        serialized_data = f'{slip.escape_characters(original_key)}|{info_after_key}'
        found_key = slip.find_border(serialized_data, 0, '|')
        parsed_key = slip.unescape_characters(found_key)
        self.assertEqual(parsed_key, original_key)

    def test_serialize(self):
        payload = {
            'str': 'A String',
            'num': 12,
            'none': None,
            'empty_obj': {},
            'encoded_str': 'Привет! Ô'
        }
        serialized_data = slip.serialize(payload)
        self.assertEqual(
            'str|l8|A String;num|n2|12;none|x;empty_obj|s0|;encoded_str|l16|Привет! Ô;',
            str(serialized_data, 'utf8')
        )

    def test_deserialize(self):
        serialized_data = slip.serialize(original_data)
        deserialized_data = slip.deserialize(serialized_data)
        self.assertEqual(original_data, deserialized_data)

    def test_deserialize_invalid(self):
        with self.assertRaises(slip.SlipError):
            empty_key = 'first_key|l4|wooo;|b1'
            slip.deserialize(bytes(empty_key, 'utf8'))

        with self.assertRaises(slip.SlipError):
            invalid_bool = 'is_good|b1;is_bad|b8;'
            slip.deserialize(bytes(invalid_bool, 'utf8'))

        with self.assertRaises(slip.SlipError):
            file_no_filename = 'str|l5|Hello;num|n1|5the-file-no-one-needs.txt;'
            slip.deserialize(bytes(file_no_filename, 'utf8'))

        with self.assertRaises(slip.SlipError):
            non_iso_date = 'who|l2|me;dob|d10|2000-07-17;'
            slip.deserialize(bytes(non_iso_date, 'utf8'))

        with self.assertRaises(slip.SlipError):
            invalid_date = 'suggestedBy|l12|Münchhausen;whatSuggested|d24|1979-05-32T12:00:00.000Z'
            slip.deserialize(bytes(invalid_date, 'utf8'))

    def attachment_fixture(self, _payload: dict, original_name: str, route: str or list):
        f_path = path.join(_.dirname(__file__), f'original_files/{original_name}')
        with open(f_path, 'rb') as file:
            f_bytes = file.read()

        payload = deepcopy(_payload)
        files_dict = {}
        _.set_v(payload, route, f_bytes)
        _.set_v(files_dict, route, original_name)

        serialized_data = slip.serialize(payload, files_dict)
        deserialized_data = slip.deserialize(serialized_data)

        parsed_file_data = _.get_v(deserialized_data, route)
        self.assertEqual(f_bytes, parsed_file_data['file'])

        parsed_dir = path.join(_.dirname(__file__), 'parsed-files')
        if not path.exists(parsed_dir):
            makedirs(parsed_dir)

        path_to_parsed_file = path.join(parsed_dir, parsed_file_data['name'])

        with open(path_to_parsed_file, 'wb+') as parsed_img_file:
            parsed_img_file.write(parsed_file_data['file'])

    def test_attachments(self):
        img_payload = {
            'user': 'Barash',
            'message': 'Беее...',
            'profile_data': {'created': datetime(2002, 11, 20)}
        }
        self.attachment_fixture(img_payload, 'good.jpg', 'profile_data.avatar')

        doc_payload = {
            'band': 'Smash Mouth',
            'date': datetime(year=2005, month=8, day=23),
            'time': '3.20'
        }
        self.attachment_fixture(doc_payload, 'doc.txt', 'text')


if __name__ == '__main__':
    unittest.main()
