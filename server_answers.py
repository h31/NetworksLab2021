import enum


class AuthAnswers(enum.Enum):
    GET_INFO = 0
    ALREADY_LOGGED = 1
    USER_DOESNT_EXIST = 2
    INCORRECT_PSWD = 3
    SUCCESS = 4


class CreateAnswers(enum.Enum):
    MISSED_VALUE = -1
    SUCCESSFUL_CREATED = 0
    USER_EXIST = 1


class PersAccAnswers(enum.Enum):
    NO_ACCESS = -1
    GET_INFO = 0
    SUCCESS = 1
    EXIT = 2
    NO_GETTER = -2
    NO_MONEY = -3
    NEGATIVE_SUM = -4
