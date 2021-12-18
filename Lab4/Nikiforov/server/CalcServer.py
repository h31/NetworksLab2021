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
    cursor.execute("SELECT COUNT(*) FROM users WHERE users.username = ? AND users.token = ?", (username, token))
    return cursor.fetchone()[0]


@get('/fast/<operation>')
@auth_basic(check)
def fast_operation(operation):
    response.content_type = 'application/json'
    try:
        args = map(float, ast.literal_eval(request.query.args))
        if operation == 'sum':
            yield json.dumps({'result': [reduce(operator.add, args)]})
        elif operation == 'mul':
            yield json.dumps({'result': [reduce(operator.mul, args)]})
        elif operation == 'sub':
            yield json.dumps({'result': [reduce(operator.sub, args)]})
        elif operation == 'div':
            try:
                yield json.dumps({'result': [reduce(operator.truediv, args)]})
            except ZeroDivisionError:
                response.status = "400 An attempt to divide by zero has been stopped"
                yield
    except ValueError:
        response.status = "400 Incorrect operand data"
        yield


@post('/result')
@auth_basic(check)
def get_result():
    operation_id = request.query.id
    response.content_type = 'application/json'
    for result in results:
        if operation_id == result['id']:
            if result['success']:
                yield json.dumps({"result": results.pop(results.index(result))['result']})  # TODO: fix this
            else:
                response.status = "400 An attempt to calculate the factorial for an unsuitable operand has been stopped"
                results.remove(result)  # TODO: and this
                yield
    response.status = "425 Not ready yet"
    yield


@get('/slow/<operation>')
@auth_basic(check)
def slow_operation(operation):
    operation_id = str(uuid.uuid4())
    if operation == 'sqrt':
        threading.Thread(target=slow_sqrt, args=(operation_id, ast.literal_eval(request.query.args))).start()
    elif operation == 'fact':
        threading.Thread(target=slow_fact, args=(operation_id, ast.literal_eval(request.query.args))).start()
    response.content_type = 'application/json'
    yield json.dumps({"id": operation_id, "message": "Accepted for processing"})


def slow_fact(operation_id, args):
    sleep(len(args) * 2)
    try:
        results.append(
            {"id": operation_id, "success": True, "result": list(map(lambda x: math.factorial(x), args))})
    except ValueError:
        results.append({"id": operation_id, "success": False, "result": []})


def slow_sqrt(operation_id, args):
    sleep(len(args) * 2)
    res = list(map(lambda x: x ** 0.5, args))
    true_res = []
    for k in res:
        true_res.append(str(k) if type(k) == complex else k)
    results.append({"id": operation_id, "success": True, "result": true_res})


@post('/login')
def login():
    credentials = request.auth
    if credentials is None:
        response.status = "401 Enter authentication data"
        yield
    elif check(*credentials):
        response.content_type = 'application/json'
        yield json.dumps({"message": f"Hello, {credentials[0]}!"})
    else:
        response.status = "404 User with these credentials doesn't exist"
        yield


def check_username(username):
    cursor.execute("SELECT COUNT(*) FROM users WHERE users.username = ?", (username,))
    return cursor.fetchone()[0]


def add_user(username, password):
    token = base64.b64encode(bytes('%s:%s' % (username, password), 'utf-8')).decode('ascii')
    cursor.execute("INSERT INTO users VALUES (?, ?)", (username, token))
    conn.commit()


@post('/register')
def register():
    username = request.query.username
    password = request.query.password
    response.content_type = 'application/json'
    if not check_username(username):
        add_user(username, password)
        yield json.dumps({"message": "Successful registration"})
    else:
        response.status = "403 Username already used"
        yield


if __name__ == '__main__':
    monkey.patch_all()
    conn = sqlite3.connect('users.sqlite')
    cursor = conn.cursor()
    results = []
    cursor.execute("CREATE TABLE IF NOT EXISTS users (username TEXT, token TEXT)")
    run(host='localhost', port=8080, server='gevent')
    conn.close()
