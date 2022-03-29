import text

text.init()


def create_update(values):
    data = {}
    for value in values:
        result = input(f'Введите {value}: ')
        if result != '':
            data[value] = result
    return data


def users(client_api):
    api = client_api
    commands = {
        '1': create_user,
        '2': get_users,
        '3': update_user,
        '4': delete_user
    }
    while True:
        command = input(text.user_command_ui)
        if command not in commands:
            break
        commands[command](api)


def create_user(api):
    answer = api.user_post(create_update(text.user_fields))
    print(answer)


def get_users(api):
    answer = api.user_get(input(text.user_by_id))
    print(answer)


def update_user(api):
    answer = api.user_update(input(text.user_by_id), create_update(text.user_fields))
    print(answer)


def delete_user(api):
    answer = api.user_delete(input(text.user_by_id))
    print(answer)


if __name__ == '__main__':
    users()
