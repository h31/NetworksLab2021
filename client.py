import time
import requests, re

reg = "(?P<op1>[0-9]+)(?P<oper>[+\-\/*!^]?)(?P<op2>[0-9]*)"
hostF = "http://localhost:8080/fast"
hostS = "http://localhost:8080/slow"

user_name = input("Имя пользователя ")
password = input("Пароль ")

fast_oper = {'+': "add", '-': "sub", '/': "div", '*': "mul"}
slow_oper = {'^': "pow", '!': "fact"}


def send(host, json, path, name, passw):
    response = requests.post(f"{host}/{path}", json=json, auth=(name, passw))
    print(response.json()["res"])


def send_long(host, json, path, name, passw):
    response = requests.post(f"{host}/{path}", json=json, auth=(name, passw))
    response_one = response.json()
    print(f"Задача обрабатывается, для проверки готовности используй номер задачи {response_one['token']}")
    json_to_send_two = {"token": int(input("Номер задачи "))}
    while True:
        response_two = requests.post(f"{host}/{path}", json=json_to_send_two, auth=(name, passw))
        if response_two.json()["status"]:
            print(response_two.json()["res"])
            break
        else:
            print("Операция выполняется")
            time.sleep(2)


while True:
    operation_sep = input("Введи операцию, пример 2+5 или 2! без пробела ")
    reGroup = re.match(reg, operation_sep)
    if reGroup["oper"] == "!":
        json_to_send = {"op1": int(reGroup.group('op1'))}
    else:
        json_to_send = {"op1": int(reGroup.group('op1')),
                        "op2": int(reGroup.group('op2'))}
    if reGroup is not None:
        if reGroup.group('oper') in fast_oper:
            send(hostF, json_to_send, fast_oper[reGroup.group('oper')], user_name, password)
        else:
            send_long(hostS, json_to_send, slow_oper[reGroup.group('oper')], user_name, password)
    else:
        print("Операция введена неверно, введите еще раз")
