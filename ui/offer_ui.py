import text

text.init()


def create_update(values):
    data = {}
    for value in values:
        result = input(f'Введите {value}: ')
        if result != '':
            data[value] = result
    return data


def offers(client_api):
    api = client_api
    commands = {
        '1': create_offer,
        '2': get_offer,
        '3': update_offer,
        '4': delete_offer,
        '5': pick_offer
    }
    while True:
        command = input(text.offer_command_ui)
        if command not in commands:
            break
        commands[command](api)


def create_offer(api):
    answer = api.offers_post(input(text.task_by_id), create_update(text.offer_fields))
    print(answer)


def get_offer(api):
    answer = api.offers_get(input(text.task_by_id), input(text.offer_by_id))
    print(answer)


def update_offer(api):
    answer = api.offers_update(
        input(text.task_by_id),
        input(text.offer_by_id),
        create_update(text.offer_fields)
    )
    print(answer)


def delete_offer(api):
    answer = api.offers_delete(input(text.task_by_id), input(text.offer_by_id))
    print(answer)


def pick_offer(api):
    answer = api.offers_pick(input(text.task_by_id), input(text.offer_by_id))
    print(answer)


if __name__ == '__main__':
    offers()