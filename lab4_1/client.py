from enum import Enum
from getpass import getpass

import re
import requests


class RequestType(Enum):
    GET = 0
    POST = 1


def make_request(request_type, route, args=None):
    try:
        if request_type == RequestType.POST:
            r = session.post("http://" + addr + "/" + route, args)
        else:
            r = session.get("http://" + addr + "/" + route, params=args)
    except requests.exceptions.ConnectionError:
        return {"status_code": 503, "data": {"answer": "Server is disconnected"}}
    return {"status_code": r.status_code, "data": r.json()}


def verify_command(string):
    string = string.strip()
    if re.fullmatch("(exit|default|ls|rate [a-zA-Z]+|convert ([0-9]+[.|])?[0-9]+( [a-zA-Z]+){2})", string):
        return string.split(" ")
    else:
        return False


if __name__ == "__main__":
    addr = input("Введите адрес сервера, для использования 192.168.56.1:5000 нажмите enter: ")
    if addr == "":
        addr = "192.168.56.1:5000"
    login = input("Логин: ")
    password = getpass(prompt='Пароль: ')  # hidden input

    session = requests.Session()
    r = make_request(RequestType.POST, "login", {'login': login, 'password': password})
    match r["status_code"]:
        case 200:
            print("Успешная авторизация")
        case 401:
            exit("Авторизация не пройдена")
        case 503:
            exit(r["data"]["answer"])
        case _:
            exit(r["data"]["answer"])

    r = make_request(RequestType.GET, "converter", {"cmd": "default"})
    if r["status_code"] == 503:
        exit(r["data"]["answer"])
    default_currency = r["data"]["answer"]

    print("\nДоступные команды: default, ls, rate <currency>, convert <amount> <from> <to>, exit")
    print("Валюта по умолчанию", default_currency)
    while True:
        cmd = verify_command(input("\n>> "))
        if cmd is False:
            print("Неверная команда")
            continue
        match cmd[0]:
            case "exit":
                break
            case "default" | "ls":
                r = make_request(RequestType.GET, "converter", {"cmd": cmd[0]})
                if r["status_code"] == 503:
                    exit(r["data"]["answer"])
                print(r["data"]["answer"])
            case "rate":
                r = make_request(RequestType.GET, "converter", {"cmd": cmd[0], "currency": cmd[1]})
                match r["status_code"]:
                    case 200:
                        print(f"{default_currency} = {r['data']['answer']} {cmd[1].upper()} ({r['data']['time']})")
                    case 503:
                        exit(r["data"]["answer"])
                    case _:
                        print(r["data"]["answer"])
            case "convert":
                r = make_request(RequestType.GET, "converter", {"cmd": cmd[0], "amount": cmd[1],
                                                                "from": cmd[2], "to": cmd[3]})
                match r["status_code"]:
                    case 200:
                        print(
                            f"{cmd[1]} {cmd[2].upper()} = {r['data']['answer']} {cmd[3].upper()} ({r['data']['time']})")
                    case 503:
                        exit(r["data"]["answer"])
                    case _:
                        print(r["data"]["answer"])

    make_request(RequestType.GET, "logout")
