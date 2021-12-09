from util.misc import *
from typing import List
from copy import deepcopy


def check_choices(data, choices):
    if not choices or includes(choices, data):
        return None
    return f'Unsupported value {data}, expected one of {", ".join(list(map(str, choices)))}'


def check_type(data, the_type):
    t_name = the_type.__name__
    article = 'an' if includes(['a', 'e', 'i', 'o', 'u'], t_name.lower()) else 'a'
    if data and get_clean_type(data) != t_name:
        return f'Expected {article} {t_name}, got {get_clean_type(data)}'
    return None


def check_fields(data: dict, _proper_fields: List[str], required: bool):
    actual_keys = list(data.keys())
    proper_fields = deepcopy(_proper_fields)
    proper_fields.sort()
    actual_keys.sort()

    check_args = (proper_fields, actual_keys) if required else (actual_keys, proper_fields)
    text = 'This field is required' if required else 'This field is not supported'
    improper_fields = difference(*check_args)
    return l_map(lambda field: f'{field}: {text}', improper_fields)


def check_required(data, required: bool):
    if data is None and required:
        return 'This field is required'
    return None


def rearrange_errors(raw_errors: dict) -> dict:
    result = deepcopy(raw_errors)
    for key, value in raw_errors.items():
        if includes(key, '.'):
            split_key = key.split('.')
            top_level_key = split_key[0]
            if result.get(top_level_key, None) is None:
                result[top_level_key] = []
            result[top_level_key].extend(
                l_map(lambda err_msg: f'{".".join(split_key[1:])}: {err_msg}', value)
            )
            result.pop(key)
    return result


def check_shape(data, shape: dict, rearrange: bool = True):
    errors = {}

    def add_err(key, val):
        nonlocal errors
        if errors.get(key, None) is None:
            errors[key] = [val]
        else:
            errors[key].append(val)

    def check_shape_recursive(_data, _shape, _add_err):
        name = _shape['name']
        top_level = _shape.get('top_level', False)

        required = _shape.get('required', False)
        required_err = check_required(_data, required)
        if required_err:
            _add_err(name, required_err)

        if _data is None:
            return

        the_type = _shape['type']
        type_err = check_type(_data, the_type)
        if type_err:
            _add_err(name, type_err)
            return

        choices = _shape.get('choices', None)
        choices_err = check_choices(_data, choices)
        if choices_err:
            _add_err(name, choices_err)
            return

        fields = _shape.get('fields', None)
        if fields:
            for field_shape in fields:
                check_shape_recursive(
                    _data.get(field_shape['name'], None),
                    field_shape,
                    lambda key, val: _add_err(f'{"" if top_level else f"{name}."}{key}', val)
                )

            proper_fields = l_map(lambda field: field['name'], fields)
            fields_err = check_fields(_data, proper_fields, False)
            for one_err in fields_err:
                _add_err(name, one_err)

    check_shape_recursive(data, shape, add_err)
    return rearrange_errors(errors) if rearrange else errors
