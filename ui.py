import phrases
from requests_api import Api


def main():
    api = Api()
    user_manager = api.User(api)
    parking_manager = api.Parking(api)
    record_manager = api.Record(api)
    jwt_manager = api.Jwt(api)
    chapter = {
        '1': [user, user_manager],
        '2': [parking, parking_manager],
        '3': [record, record_manager],
        '4': [jwt, jwt_manager]
    }
    while True:
        cmd = input(phrases.main_menu)
        if cmd in chapter:
            run = chapter[cmd]
            run[0](run[1])


def show_info(response, indent_count=1):
    indent = '    ' * indent_count
    print()
    if type(response) is dict:
        for key, value in response.items():
            if (type(value) is list) or (type(value) is dict):
                print(f'{indent}{key}:')
                show_info(value, indent_count + 1)
            else:
                print(f'{indent}{key}: {value}')
    if type(response) is list:
        for k in response:
            if type(k) is dict:
                for key, value in k.items():
                    if (type(value) is list) or (type(value) is dict):
                        print(f'{indent}{key}:')
                        show_info(value, indent_count + 1)
                    else:
                        print(f'{indent}{key}: {value}')
                print()
            else:
                print(f'{indent} {k}')
    print()


def user(user_manager):
    cmd = {
        'create': user_manager.post_user,
        'read': user_manager.get_user,
        'change': user_manager.update_user,
        'delete': user_manager.delete_user
    }
    while True:
        print(phrases.command_from_list)
        command = input(f'{list(cmd.keys())}\n')
        if command == 'exit':
            break
        if command not in cmd:
            continue
        show_info(cmd[command]())


def parking(parking_manager):
    cmd = {
        'create': parking_manager.post_parking,
        'read': parking_manager.get_parking,
        'update': parking_manager.update_parking,
        'delete': parking_manager.delete_parking
    }
    while True:
        print(phrases.command_from_list)
        command = input(f'{list(cmd.keys())}\n')
        if command == 'exit':
            break
        if command not in cmd:
            continue
        show_info(cmd[command]())


def record(record_manager):
    cmd = {
        'create': record_manager.post_record,
        'read': record_manager.get_record,
        'change': record_manager.update_record,
        'delete': record_manager.delete_record
    }
    while True:
        print(phrases.command_from_list)
        command = input(f'{list(cmd.keys())}\n')
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
        print(phrases.command_from_list)
        command = input(f'{list(cmd.keys())}\n')
        if command == 'exit':
            break
        if command not in cmd:
            continue
        show_info(cmd[command]())


if __name__ == '__main__':
    main()
