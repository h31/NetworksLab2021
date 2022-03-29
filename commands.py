import enum

class Command(enum.Enum):
    EXIT = 'exit'
    REGISTER = 'register'
    ADD_TASK = 'add_task'
    MY_TASKS = 'my_tasks'
    ALL_TASKS = 'all_tasks'
    UPDATE_TASK = 'update_task'
    LOGOUT = 'logout'
    LOGIN = 'login'
