import json
import math
import threading
import time

from bottle import route, run, request, auth_basic

possible_names = ["u1", "u2", ""]
posible_passwords = ["pas", "word", ""]
timeout = 3
dict_of_res = {}
number = 1


def check_pass(username, password):
    return username in possible_names and password in posible_passwords


def pow_processing(req, token):
    time.sleep(10)
    global dict_of_res, number
    res = math.pow(req['op1'], req['op2'])
    dict_of_res[token] = {"status": "ready", "res": res}
    print(f"for power with token {token} result is {res}")


def fact_processing(req, token):
    time.sleep(10)
    global dict_of_res, number
    res = math.factorial(req['op1'])
    dict_of_res[token] = {"status": "ready", "res": res}
    print(f"for factorial with token {token} result is {res}")


@route('/fast/add', method=['GET', 'POST'])
@auth_basic(check_pass)
def addition():
    posted = json.loads(request.body.getvalue().decode("utf-8"))
    rez = posted["op1"] + posted["op2"]
    posted["rez"] = rez
    print(rez)
    return posted


@route('/fast/sub', method=['GET', 'POST'])
@auth_basic(check_pass)
def subtraction():
    posted = json.loads(request.body.getvalue().decode("utf-8"))
    rez = posted["op1"] - posted["op2"]
    posted["rez"] = rez
    print(rez)
    return posted


@route('/fast/div', method=['GET', 'POST'])
@auth_basic(check_pass)
def division():
    posted = json.loads(request.body.getvalue().decode("utf-8"))
    rez = posted["op1"] / posted["op2"]
    posted["rez"] = rez
    print(rez)
    return posted


@route('/fast/mul', method=['GET', 'POST'])
@auth_basic(check_pass)
def multiplication():
    posted = json.loads(request.body.getvalue().decode("utf-8"))
    rez = posted["op1"] * posted["op2"]
    posted["rez"] = rez
    print(rez)
    return posted


@route('/slow/fact', method=['GET', 'POST'])
@auth_basic(check_pass)
def factorial():
    global number, dict_of_res
    posted = json.loads(request.body.getvalue().decode("utf-8"))
    if 'token' not in posted.keys():
        number = number + 1
        dict_of_res[number-1] = {"status": "work is started"}
        fact_th = threading.Thread(target=fact_processing, args=(posted, number - 1))
        fact_th.start()
        return {'token': number - 1}
    else:
        if dict_of_res[posted['token']]["status"] != 'ready':
            dict_of_res[posted['token']] = {"status": "in progress"}
        return dict_of_res[posted['token']]


@route('/slow/pow', method=['GET', 'POST'])
@auth_basic(check_pass)
def power():
    global number, dict_of_res
    posted = json.loads(request.body.getvalue().decode("utf-8"))
    if 'token' not in posted.keys():
        number = number + 1
        dict_of_res[number-1] = {"status": "work is started"}
        fact_th = threading.Thread(target=pow_processing, args=(posted, number - 1))
        fact_th.start()
        return {'token': number - 1}
    else:
        if dict_of_res[posted['token']]["status"] != 'ready':
            dict_of_res[posted['token']] = {"status": "in progress"}
        return dict_of_res[posted['token']]


run(host='localhost', port=8080)
