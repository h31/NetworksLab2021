import re
import requests
from enum import Enum
from getpass import getpass


def get_status():
    r = make_request(RequestType.GET, "status").json()
    print(f"Пользователь: {r['username']}; ", end="")
    if r["is_croupier"]:
        print("крупье")
    else:
        print(f"на счету: {r['money']}")


def get_results():
    r = make_request(RequestType.GET, "results")
    match r.status_code:
        case 200:
            answer = r.json()
            print(f"Результат розыгрыша: {answer['number']}")
            for i in range(len(answer["usernames"])):
                match answer["types"][i]:
                    case -2:
                        _type = "четное"
                    case -1:
                        _type = "нечетное"
                    case _:
                        _type = str(answer["types"][i])
                print(f"Игрок: {answer['usernames'][i]}, " +
                      f"Ставка: {str(answer['amounts'][i])}, " +
                      f"Тип ставки: {_type} -> Итог: {answer['results'][i]}")
        case 204:
            print("Пока нет результатов")
        case _:
            print("Ошибка сервера")


def get_bets():
    r = make_request(RequestType.GET, "bets")
    match r.status_code:
        case 200:
            answer = r.json()
            for i in range(len(answer["usernames"])):
                match answer["types"][i]:
                    case -2:
                        _type = "четное"
                    case -1:
                        _type = "нечетное"
                    case _:
                        _type = str(answer["types"][i])
                print(f"Игрок: {answer['usernames'][i]}, " +
                      f"Ставка: {str(answer['amounts'][i])}, Тип ставки: {_type}")
        case 204:
            print("Пока нет ставок")
        case _:
            print("Ошибка сервера")


def new_bet(amount, _type):
    r = make_request(RequestType.POST, "new", {"amount": amount, "type": _type})
    match r.status_code:
        case 200:
            print("Ставка принята")
        case 444:
            print("Ошибка: некорректная ставка")
        case 445:
            print("Ошибка: для указанной ставки недостаточно денег")
        case _:
            print("Ошибка сервера")


def spin():
    r = make_request(RequestType.GET, "spin")
    if r.status_code == 200:
        print(f"Результат розыгрыша: {r.text}")
    else:
        print("Ошибка сервера")


class RequestType(Enum):
    GET = 1
    POST = 2


def make_request(requestType, route, args=None):
    try:
        if requestType == RequestType.GET:
            r = session.get(addr + route, params=args)
        else:
            r = session.post(addr + route, args)
        if r.status_code == 401:
            exit("Авторизация не пройдена")
        else:
            return r
    except (requests.exceptions.MissingSchema, requests.exceptions.InvalidSchema):
        exit("Некорректный адрес сервера")
    except requests.exceptions.ConnectionError:
        exit("Потеряна связь с сервером")


def verify_command(string):
    m = re.match("^(exit|status|results|bets|spin)$", string)
    if m is not None:
        return [string]

    m = re.match("^new(\s)+(?P<amount>[0-9]+)(\s)+(?P<type>-[12]|[0-9]|[1-2][0-9]|3[0-6])$", string)
    if m is not None:
        return ["new", m.group("amount"), m.group("type")]

    return False


if __name__ == "__main__":
    addr = input("Сервер (по умолчанию http://127.0.0.1:5000/): ")
    if len(addr) == 0:
        addr = "http://127.0.0.1:5000/"
    login = input("Логин: ")
    password = getpass(prompt='Пароль: ')
    croupier = input("Войти как крупье? (y/n): ").upper() == "Y"

    session = requests.Session()
    r = make_request(RequestType.POST, "login",
                     {"login": login, "password": password, "is_croupier": croupier})
    match r.status_code:
        case 200:
            print("Авторизация пройдена")
        case 441:
            exit("Ошибка: пользователь под таким логином уже авторизован")
        case 435:
            exit("Ошибка: крупье уже авторизован")
        case _:
            exit("Ошибка сервера")

    print("\nКоманды: exit, status, results, bets, ", end="")
    if croupier:
        print("spin")
    else:
        print("new <ставка> <тип>")
        print("Виды ставок: -2 (четное), -1 (нечетное), 0-36")

    while True:
        cmd = verify_command(input("\n>> ").strip())
        if cmd is False:
            print("Некорректная команда")
            continue
        match cmd[0]:
            case "exit":
                make_request(RequestType.GET, "logout")
                session.close()
                break
            case "status":
                get_status()
            case "results":
                get_results()
            case "bets":
                get_bets()
            case "new":
                if not croupier:
                    new_bet(cmd[1], cmd[2])
                else:
                    print("Ошибка: вы крупье, ваши ставки не принимаются")
            case "spin":
                if croupier:
                    spin()
                else:
                    print("Ошибка: вы не крупье")
