import util.misc as _
from typing import List
from copy import copy


def check_choices(data, choices):
    if not choices or _.includes(choices, data):
        return None
    return f'Unsupported value {data}, expected one of ${", ".join(list(map(str,choices)))}'


def check_type(data, the_type):
    t_name = the_type.__name__
    article = 'an' if _.includes(['a', 'e', 'i', 'o', 'u'], t_name.lower()) else 'a'
    if data and _.get_clean_type(data) != t_name:
        return f'Expected {article} ${t_name}, got ${_.get_clean_type(data)}'
    return None


def check_fields(data: dict, _proper_fields: List[str], required: bool):
    actual_keys = list(data.keys())
    proper_fields = copy(_proper_fields)
    proper_fields.sort()
    actual_keys.sort()

    check_args = (proper_fields, actual_keys) if required else (actual_keys, proper_fields)
    text = 'This field is required' if required else 'This field is not supported'
    improper_fields = _.difference(*check_args)
    return list(map(lambda field: f'{field}: {text}', improper_fields))


def check_required(data, required: bool):
    if data is None and required:
        return 'This field is required'
    return None



