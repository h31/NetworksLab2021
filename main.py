from api import Api


def main():
    api = Api()
    user_manager = api.User(api)
    meter_manager = api.Meter(api)
    reading_manager = api.Reading(api)
    jwt_manager = api.Jwt(api)
    chapter = {
        '1': [user, user_manager],
        '2': [meter, meter_manager],
        '3': [reading, reading_manager],
        '4': [jwt, jwt_manager]
    }
    while True:
        cmd = input(
            'Сервис Коммунальных платежей\n\n\n'
            'Перед использованием сервиса может потребоваться создание нового'
            ' пользователя или обновление токена в соответствующих '
            'разделах\n\n'
            'Выберете необходимый раздел: \n\n'
            '1 - Раздел пользователей\n'
            '2 - Раздел счетчиков\n'
            '3 - Раздел записей\n'
            '4 - Раздел токенов\n'
        )
        if cmd in chapter:
            run = chapter[cmd]
            run[0](run[1])


def show_info(response):
    print()
    if type(response) is dict:
        for key, value in response.items():
            print(f'    {key}: {value}')
    if type(response) is list:
        for k in response:
            for key, value in k.items():
                print(f'    {key}: {value}')
            print()
    print()


def user(user_manager):
    cmd = {
        'create': user_manager.post_user,
        'read': user_manager.get_user,
        'change': user_manager.update_user,
        'delete': user_manager.delete_user
    }
    while True:
        command = input(
            'Раздел пользователей\n'
            f'Введите команду из списка {list(cmd.keys())}\n'
            'Также вы можете написать exit для выхода из раздела:\n\n'
        )
        if command == 'exit':
            break
        if command not in cmd:
            continue
        show_info(cmd[command]())


def meter(meter_manager):
    cmd = {
        'create': meter_manager.post_meter,
        'read': meter_manager.get_meter,
        'delete': meter_manager.delete_meter
    }
    while True:
        command = input(
            'Раздел счетчиков\n'
            f'Введите команду из списка {list(cmd.keys())}\n'
            'Также вы можете написать exit для выхода из раздела:\n\n'
        )
        if command == 'exit':
            break
        if command not in cmd:
            continue
        show_info(cmd[command]())


def reading(reading_manager):
    cmd = {
        'read': reading_manager.get_reading,
        'change': reading_manager.update_reading,
        'delete': reading_manager.delete_reading
    }
    while True:
        command = input(
            'Раздел записей\n'
            f'Введите команду из списка {list(cmd.keys())}\n'
            'Также вы можете написать exit для выхода из раздела:\n\n'
        )
        if command == 'exit':
            break
        if command not in cmd:
            continue
        show_info(cmd[command]())


def jwt(jwt_manager):
    cmd = {
        'create': jwt_manager.create,
        'refresh': jwt_manager.refresh,
    }
    while True:
        command = input(
            'Раздел токенов\n'
            f'Введите команду из списка {list(cmd.keys())}\n'
            'Также вы можете написать exit для выхода из раздела:\n\n'
        )
        if command == 'exit':
            break
        if command not in cmd:
            continue
        show_info(cmd[command]())


if __name__ == '__main__':
    main()