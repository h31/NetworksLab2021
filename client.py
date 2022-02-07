import threading
import time
import requests, re

user_is_login = False
regex = '[0-9]+[+\-\/*!^]{1}[0-9]*'
reg = "(?P<op1>[0-9]+)(?P<oper>[+\-\/*!^]?)(?P<op2>[0-9]*)"
hostF = "http://localhost:8080/fast"
hostS = "http://localhost:8080/slow"

user_name = input("Имя пользователя ")
password = input("Пароль ")


def sending(host, json, path, name, passw):
    response = requests.post(f"{host}/{path}", json=json, auth=(name, passw))
    print(response.json()["rez"])


while True:
    operation_sep = input("Введи операцию, пример 2+5 или 2! без пробела ")
    reGroup = re.match(reg, operation_sep)
    if reGroup["oper"] == "!":
        json_to_send = {"op1": int(reGroup.group('op1'))}
    else:
        json_to_send = {"op1": int(reGroup.group('op1')),
                        "op2": int(reGroup.group('op2'))}
    if reGroup is not None:
        if "+" == reGroup.group('oper'):
            sending(hostF, json_to_send, "add", user_name, password)
        if "-" in operation_sep:
            sending(hostF, json_to_send, "sub", user_name, password)
        if "/" in operation_sep:
            sending(hostF, json_to_send, "div", user_name, password)
        if "*" in operation_sep:
            sending(hostF, json_to_send, "mul", user_name, password)
        if "!" in operation_sep:
            response = requests.post(f"{hostS}/fact", json=json_to_send, auth=(user_name, password))
            response_one = response.json()
            print(f"Задача обрабатывается, для проверки готовности используй номер задачи {response_one['token']}")
            json_to_send_two = {"token": int(input("Номер задачи "))}
            while True:
                response_two = requests.post(f"{hostS}/fact", json=json_to_send_two, auth=(user_name, password))
                if response_two.json()["status"] == 'ready':
                    print(response_two.json()["res"])
                    break
                else:
                    print(response_two.json()["status"])
                    time.sleep(2)
        if "^" in operation_sep:
            response = requests.post(f"{hostS}/pow", json=json_to_send, auth=(user_name, password))
            response_one = response.json()
            print(f"Задача обрабатывается, для проверки готовности используй номер задачи {response_one['token']}")
            json_to_send_two = {"token": int(input("Номер задачи "))}
            while True:
                response_two = requests.post(f"{hostS}/pow", json=json_to_send_two, auth=(user_name, password))
                if response_two.json()["status"] == 'ready':
                    print(response_two.json()["res"])
                    break
                else:
                    print(response_two.json()["status"])
                    time.sleep(2)
    else:
        print("Операция введена неверно, введите еще раз")