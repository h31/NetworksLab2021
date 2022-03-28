from time import sleep

from api import Api
import text
from ui.jwt_ui import jwt
from ui.user_ui import users
from ui.task_ui import tasks
from ui.offer_ui import offers

text.init()


def main():
    api = Api()
    commands = {
        '1': users,
        '2': tasks,
        '3': offers,
        '4': jwt,
    }
    while True:
        sleep(0.5)
        print(text.main_menu)
        command = input()
        if command not in commands:
            break
        commands[command](api)


if __name__ == '__main__':
    main()