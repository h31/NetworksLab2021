from datetime import datetime
from os import path
from re import split, sub, match
from typing import TypeVar, List, Callable


T = TypeVar('T')


def get_clean_type(val) -> str:
    return type(val).__name__


def includes(search_in, val) -> bool:
    s = search_in if type(search_in) == str else list(search_in)
    return s.count(val) != 0


def find(arr: List[T], comparator: Callable[[T, int, List[T]], bool]) -> T:
    for i, item in enumerate(arr):
        if comparator(item, i, arr):
            return item
    return None


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
    return datetime.fromisoformat(datetime.utcnow().isoformat(timespec='milliseconds'))


def format_time(dt: datetime = None) -> str:
    _dt = dt if dt else now()
    res = _dt.strftime('%H:%M:%S.')
    res += str(round(_dt.microsecond / 1000))
    return f'[{res}]'


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


def difference(arr: List[T], other: List[T]) -> List[T]:
    return list(filter(lambda item: not includes(other, item), arr))


def l_map(mapper, collection):
    return list(map(mapper, collection))


def l_filter(filter_func, collection):
    return list(filter(filter_func, collection))


def is_empty(obj) -> bool:
    if not obj:
        return True
    return not bool(len(obj.keys()))


def repl_snake(match_obj):
    m = match_obj.group()
    start = f'{m[0]}_' if not includes(['-', ' '], m[0]) else '_'
    return f'{start}{m[1].lower()}'


def snake_case(text: str) -> str:
    return sub(
        pattern=r'([- ]\w)|([a-z\d][A-Z])',
        repl=repl_snake,
        string=text
    ).lower()


def w_amount(amt: int, text: str):
    _text = text
    ending = ''
    if amt != 1:
        if text.endswith('y'):
            _text = text[:len(text) - 1]
            ending = 'ies'
        elif match(r'^[a-zA-Z]{2,}[qwrtpsdfghjklzxcvbnm][uo]s$', text):
            ending = 'i'
            _text = text[:len(text) - 2]
        elif text.endswith('s'):
            ending = 'es'
        else:
            ending = 's'

    return f'{amt} {_text}{ending}'
