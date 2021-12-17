import ast
import base64
import json
import math
import operator
import sqlite3
import threading
import uuid
from functools import reduce

from bottle import request, response, auth_basic, get, post, run
from gevent import monkey, sleep


def check(username, password):
    token = base64.b64encode(bytes('%s:%s' % (username, password), 'utf-8')).decode('ascii')
    cursor.execute(f"SELECT COUNT(*) FROM users WHERE users.username = '{username}' AND users.token = '{token}';")
    return cursor.fetchone()[0]


@get('/fast/<operation>')
@auth_basic(check)
def fast_operation(operation):
    response.content_type = 'application/json'
    if operation == 'sum':
        yield repr([reduce(operator.add, ast.literal_eval(request.query.args))]).encode()
    elif operation == 'mul':
        yield repr([reduce(operator.mul, ast.literal_eval(request.query.args))]).encode()
    elif operation == 'sub':
        yield repr([reduce(operator.sub, ast.literal_eval(request.query.args))]).encode()
    elif operation == 'div':
        try:
            yield repr([reduce(operator.truediv, ast.literal_eval(request.query.args))]).encode()
        except ZeroDivisionError:
            response.status = 400
            yield json.dumps({"message": "An attempt to divide by zero has been stopped!"})


@get('/result')
@auth_basic(check)
def get_result():
    operation_id = request.query.id
    response.content_type = 'application/json'
    for result in results:
        if operation_id == result['id']:
            response.status = 200 if result['success'] else 400
            return json.dumps({"result": results.pop(results.index(result))['result']})
    return json.dumps({"result": "Not ready yet"})


def check_results(operation_id):
    for result in results:
        if operation_id == result['id']:
            return True
    return False


def add_result(result):
    if result not in results:
        results.append(result)


@get('/slow/<operation>')
@auth_basic(check)
def slow_operation(operation):
    operation_id = str(uuid.uuid4())
    response.content_type = 'application/json'
    if operation == 'sqrt':
        threading.Thread(target=slow_sqrt, args=(operation_id, ast.literal_eval(request.query.args))).start()
    elif operation == 'fact':
        threading.Thread(target=slow_fact, args=(operation_id, ast.literal_eval(request.query.args))).start()
    yield json.dumps({"id": operation_id, "message": "Accepted for processing"})


def slow_fact(operation_id, args):
    if not check_results(operation_id):
        sleep(len(args) * 2)
        try:
            add_result(
                {"id": operation_id, "success": True, "result": repr(list(map(lambda x: math.factorial(x), args)))})
        except ValueError:
            add_result({"id": operation_id, "success": False,
                        "result": "An attempt to calculate the factorial for an unsuitable operand has been stopped!"})


def slow_sqrt(operation_id, args):
    if not check_results(operation_id):
        sleep(len(args) * 2)
        add_result({"id": operation_id, "success": True, "result": repr(list(map(lambda x: x ** 0.5, args)))})


@post('/login')
def login():
    credentials = request.auth
    if credentials is None:
        response.status = 401
        response.content_type = 'application/json'
        yield json.dumps({"success": False, "message": "Enter authentication data!"})
    elif check(*credentials):
        response.content_type = 'application/json'
        yield json.dumps({"success": True, "message": f"Hello, {credentials[0]}!"})
    else:
        response.status = 404
        response.content_type = 'application/json'
        yield json.dumps({"success": False, "message": "User with these credentials doesn't exist"})


def check_username(username):
    cursor.execute(f"SELECT COUNT(*) FROM users WHERE users.username = '{username}';")
    return cursor.fetchone()[0]


def add_user(username, password):
    token = base64.b64encode(bytes('%s:%s' % (username, password), 'utf-8')).decode('ascii')
    cursor.execute(f"INSERT INTO users VALUES ('{username}', '{token}');")
    conn.commit()


@post('/register')
def register():
    username = request.query.username
    password = request.query.password
    response.content_type = 'application/json'
    if not check_username(username):
        add_user(username, password)
        yield json.dumps({"success": True, "message": "Successful registration"})
    else:
        response.status = 403
        yield json.dumps({"success": False, "message": "Username already used"})


if __name__ == '__main__':
    monkey.patch_all()
    conn = sqlite3.connect('users.sqlite')
    cursor = conn.cursor()
    results = []
    cursor.execute("CREATE TABLE IF NOT EXISTS users (username TEXT, token TEXT);")
    run(host='localhost', port=8080, server='gevent')
    conn.close()
