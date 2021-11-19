from datetime import datetime
from os import path
from re import split


def get_clean_type(val) -> str:
    return type(val).__name__


def includes(list_like, val) -> bool:
    return list(list_like).count(val) != 0


def invert(initial: dict) -> dict:
    res = {}
    for key, value in initial.items():
        res[str(value)] = key
    return res


def num(text: str) -> int or float:
    try:
        return int(text)
    except ValueError:
        return float(text)


def now() -> datetime:
    raw = datetime.utcnow()
    return raw.replace(microsecond=round(raw.microsecond / 1000) * 1000)


def dirname(file: str) -> str:
    return path.dirname(path.realpath(file))


def from_b(b: bytes) -> str:
    return b.decode(encoding='utf8', errors='replace')


def to_b(s: str) -> bytes:
    return bytes(s, 'utf8')


def get_v(data: dict or list, route: str or list):
    r_parts = split(r'[.\]\[]', route) if type(route) == str else route
    if len(r_parts) == 1:
        key = r_parts[0] if type(data) == dict else int(r_parts[0])
        return data[key]
    else:
        return get_v(data[r_parts[0]], r_parts[1:])


def set_v(data: dict or list, route: str or list, val):
    r_parts = split(r'[.\]\[]', route) if type(route) == str else route
    curr = r_parts[0]
    if len(r_parts) == 1:
        if type(data) == list:
            data.insert(int(curr), val)
        else:
            data[curr] = val
    else:
        if type(data) == list:
            try:
                scope = data[curr]
            except IndexError:
                scope = None
        else:
            scope = data.get(curr, None)
        if scope is None:
            container = [] if type(r_parts[1]) == int else {}
            if type(data) == list:
                data.insert(int(curr), container)
            else:
                data[curr] = container
        set_v(data[curr], r_parts[1:], val)
