import text

text.init()


def create_update(values):
    data = {}
    for value in values:
        result = input(f'Введите {value}: ')
        if result != '':
            data[value] = result
    return data


def tasks(client_api):
    api = client_api
    commands = {
        '1': create_task,
        '2': get_task,
        '3': update_task,
        '4': change_status_task,
        '5': delete_task,
    }
    while True:
        command = input(text.task_command_ui)
        if command not in commands:
            break
        commands[command](api)


def create_task(api):
    answer = api.tasks_post(create_update(text.task_fields))
    print(answer)


def get_task(api):
    answer = api.tasks_get(input(text.task_by_id))
    print(answer)


def update_task(api):
    answer = api.tasks_update(input(text.task_by_id), create_update(text.task_fields))
    print(answer)


def change_status_task(api):
    answer = api.tasks_change_status(input(text.task_by_id), {'status': input(text.task_status)})
    print(answer)


def delete_task(api):
    answer = api.tasks_delete(input(text.task_by_id))
    print(answer)


if __name__ == '__main__':
    tasks()