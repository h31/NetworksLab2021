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
    dict_of_res[token] = {"status": True, "res": res}
    print(f"for power with token {token} result is {res}")


def fact_processing(req, token):
    time.sleep(10)
    global dict_of_res, number
    res = math.factorial(req['op1'])
    dict_of_res[token] = {"status": True, "res": res}
    print(f"for factorial with token {token} result is {res}")


def slow(oper):
    global number, dict_of_res
    posted = request.json
    if 'token' not in posted.keys():
        number += 1
        dict_of_res[number - 1] = {"status": False}
        if oper == 'fact':
            fact_th = threading.Thread(target=fact_processing, args=(posted, number - 1))
            fact_th.start()
        else:
            pow_th = threading.Thread(target=pow_processing, args=(posted, number - 1))
            pow_th.start()
        return {'token': number - 1}
    else:
        return dict_of_res[posted['token']]


def fast(oper):
    posted = request.json
    if oper == "add":
        posted["res"] = posted["op1"] + posted["op2"]
        return posted
    elif oper == "sub":
        posted["res"] = posted["op1"] - posted["op2"]
        return posted
    elif oper == 'div':
        posted["res"] = posted["op1"] / posted["op2"]
        return posted
    else:
        posted["res"] = posted["op1"] * posted["op2"]
        return posted


@route('/slow/<operation>', method=['POST'])
@auth_basic(check_pass)
def slow_operation(operation):
    return slow(operation)


@route('/fast/<operation>', method=['POST'])
@auth_basic(check_pass)
def slow_operations(operation):
    return fast(operation)


run(host='localhost', port=8080)
