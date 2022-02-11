import re
from getpass import getpass

import requests


def verify_command(string):
    m = re.match("(?P<cmd>exit|default|ls)", string)
    if m is not None:
        return [m.group("cmd")]

    m = re.match("rate( )+(?P<currency>[A-Za-z]+)", string)
    if m is not None:
        return ["rate", m.group("currency")]

    m = re.match("convert( )+(?P<amount>([0-9]+[.|])?[0-9]+)( )+(?P<from>[A-Za-z]+)( )+(?P<to>[A-Za-z]+)", string)
    if m is not None:
        return ["convert", m.group("amount"), m.group("from"), m.group("to")]

    return False


def get_request(route, params=None):
    try:
        r = session.get(addr + route, params=params)
        if r.status_code == 401:
            exit("Login failed")
        else:
            return r
    except requests.exceptions.ConnectionError:
        exit("Server is disconnected")


def post_request(route, args=None):
    try:
        r = session.post(addr + route, args)
        if r.status_code == 401:
            exit("Login failed")
        else:
            return r
    except requests.exceptions.ConnectionError:
        exit("Server is disconnected")


if __name__ == "__main__":
    addr = input("Введите адрес сервера, для использования http://192.168.56.1:5000/ нажмите enter: ")
    if addr == "":
        addr = "http://192.168.56.1:5000/"
    login = input("Логин: ")
    password = getpass(prompt='Пароль: ')  # hidden input

    session = requests.Session()
    r = post_request("login", {'login': login, 'password': password})
    print("Успешная авторизация")

    r = get_request("default")
    default_currency = r.text

    print("\nДоступные команды: default, ls, rate <currency>, convert <amount> <from> <to>, exit")
    print("Валюта по умолчанию", default_currency)
    while True:
        cmd = verify_command(input("\n>> ").strip())
        if cmd is False:
            print("Неверная команда")
            continue
        match cmd[0]:
            case "exit":
                break
            case "default" | "ls":
                r = get_request(cmd[0])
                if r.status_code != 200:
                    print("Error: ", end="")
                print(r.text)
            case "rate":
                r = get_request("rate", {"currency": cmd[1]})
                if r.status_code != 200:
                    print(f"Error: {r.text}")
                else:
                    answer = r.json()
                    print(f"1 {default_currency} = {answer['rate']} {cmd[1].upper()} ({answer['time']})")
            case "convert":
                r = get_request("convert", {"amount": cmd[1], "from": cmd[2], "to": cmd[3]})
                if r.status_code != 200:
                    print(f"Error: {r.text}")
                else:
                    answer = r.json()
                    print(f"{cmd[1]} {cmd[2].upper()} = {answer['amount']} {cmd[3].upper()} ({answer['time']})")

    session.get(addr + "logout")
