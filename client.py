import threading

import requests, re

user_is_login = False
regex = '[0-9]+[+\-\/*!^]{1}[0-9]*'
hostF = "http://localhost:8080/fast"
hostS = "http://localhost:8080/slow"

user_name = input("Имя пользователя ")
password = input("Пароль ")

#def handler():
while True:
    operation_sep = input("Введи операцию, пример 2+5 или 2! без пробела ")
    json_to_send = {}
    if re.match(regex, operation_sep) is not None:
        if "+" in operation_sep:
            json_to_send = {"op1": int(operation_sep[0:operation_sep.find("+")]),
                            "op2": int(operation_sep[operation_sep.find("+") + 1:])}
            response = requests.post(f"{hostF}/add", json=json_to_send, auth=(user_name, password))
            print(response.json()["rez"])
        if "-" in operation_sep:
            json_to_send = {"op1": int(operation_sep[0:operation_sep.find("-")]),
                            "op2": int(operation_sep[operation_sep.find("-") + 1:])}
            response = requests.post(f"{hostF}/sub", json=json_to_send, auth=(user_name, password))
            print(response.json()["rez"])
        if "/" in operation_sep:
            json_to_send = {"op1": float(operation_sep[0:operation_sep.find("/")]),
                            "op2": float(operation_sep[operation_sep.find("/") + 1:])}
            response = requests.post(f"{hostF}/div", json=json_to_send, auth=(user_name, password))
            print(response.json()["rez"])
        if "*" in operation_sep:
            json_to_send = {"op1": int(operation_sep[0:operation_sep.find("*")]),
                            "op2": int(operation_sep[operation_sep.find("*") + 1:])}
            response = requests.post(f"{hostF}/mul", json=json_to_send, auth=(user_name, password))
            print(response.json()["rez"])
        if "!" in operation_sep:
            # json_to_send = {"op1": int(operation_sep[0:operation_sep.find("!")])}
            # response = requests.post(f"{hostS}/fact", json=json_to_send, auth=(user_name, password))
            # print(response.json()["rez"])
            json_to_send = {"op1": int(operation_sep[0:operation_sep.find("!")])}
            response = requests.post(f"{hostS}/fact", json=json_to_send, auth=(user_name, password))
            response_one = response.json()
            response_two = requests.post(f"{hostS}/fact", json=response_one, auth=(user_name, password))
            print(response_two.json()["rez"])
        if "^" in operation_sep:
            # json_to_send = {"op1": int(operation_sep[0:operation_sep.find("^")]),
            #                 "op2": int(operation_sep[operation_sep.find("^") + 1:])}
            # response = requests.post(f"{hostS}/pow", json=json_to_send, auth=(user_name, password))
            # print(response.json()["rez"])
            json_to_send = {"op1": int(operation_sep[0:operation_sep.find("^")]),
                            "op2": int(operation_sep[operation_sep.find("^") + 1:])}
            response = requests.post(f"{hostS}/pow", json=json_to_send, auth=(user_name, password))
            response_one = response.json()
            response_two = requests.post(f"{hostS}/pow", json=response_one, auth=(user_name, password))
            print(response_two.json()["rez"])
    else:
        print("Операция введена неверно, введите еще раз")

#thread = threading.Thread(target=handler())
#thread.start()

#handler()