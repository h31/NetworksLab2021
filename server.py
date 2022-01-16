import json, math, time
import threading

from bottle import route, run, request, auth_basic

possible_names = ["u1", "u2", ""]
posible_passwords = ["pas", "word", ""]
results = []


still_in_process = []
number = 0


def check_pass(username, password):
    return username in possible_names and password in posible_passwords



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
    global number, still_in_process
    posted = json.loads(request.body.getvalue().decode("utf-8"))
    if len(posted) < 2:
        number += 1
        still_in_process.append(False)
        posted["number"] = number
        time.sleep(1)
    else:
        rez = math.factorial(posted["op1"])
        still_in_process[posted['number'] - 1] = True
        if still_in_process[posted['number'] - 1]:
            posted["rez"] = rez
        else:
            posted["rez"] = "mistake"
    return posted


@route('/slow/pow', method=['GET', 'POST'])
@auth_basic(check_pass)
def power():
    # posted = json.loads(request.body.getvalue().decode("utf-8"))
    # rez = math.pow(posted["op1"], posted["op2"])
    # posted["rez"] = rez
    # print(rez)
    # return posted
    global number, still_in_process
    posted = json.loads(request.body.getvalue().decode("utf-8"))
    if len(posted) < 3:
        number += 1
        still_in_process.append(False)
        posted["number"] = number
        time.sleep(1)
    else:
        rez = math.pow(posted["op1"], posted["op2"])
        still_in_process[posted['number'] - 1] = True
        if still_in_process[posted['number'] - 1]:
            posted["rez"] = rez
        else:
            posted["rez"] = "mistake"
    return posted

# add_th = threading.Thread(target=addition)
# add_th.start()
#
# sub_th = threading.Thread(target=subtraction)
# sub_th.start()
#
# div_th = threading.Thread(target=division)
# div_th.start()
#
# mul_th = threading.Thread(target=multiplication)
# mul_th.start()
#
# fact_th = threading.Thread(target=factorial)
# fact_th.start()
#
# pow_th = threading.Thread(target=power)
# pow_th.start()


run(host='localhost', port=8080)

# словарь в котором ключ это номерок а значение готово ли, ключ это время а значение это время старта
