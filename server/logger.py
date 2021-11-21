from colorama import init as init_c, Fore
import util.misc as _
from enum import Enum


class Status(Enum):
    success = Fore.GREEN
    error = Fore.RED
    info = Fore.BLUE
    warn = Fore.YELLOW
    plain = Fore.WHITE
    prefix = Fore.CYAN


class OccasionType(Enum):
    action = 'Action'
    error = 'Error'


def init():
    init_c(autoreset=True, convert=True)


def log(occasion_type: str = None, occasion_name: str = None, comment: str = None, status: Status = Status.plain):
    to_log = [_.format_time()]

    if occasion_name and occasion_type:
        to_log.extend([f'  {occasion_type}: ', status.value + occasion_name])

    if comment:
        to_log.extend(['  --> ', status.value + comment])

    print(''.join(to_log))

