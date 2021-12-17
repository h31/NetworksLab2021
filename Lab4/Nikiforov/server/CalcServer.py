import ast
import base64
import json
import math
import operator
import sqlite3
import threading
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
    if operation == 'sum':
        response.content_type = 'application/json'
        yield repr([reduce(operator.add, ast.literal_eval(request.query.args))]).encode()
    elif operation == 'mul':
        response.content_type = 'application/json'
        yield repr([reduce(operator.mul, ast.literal_eval(request.query.args))]).encode()
    elif operation == 'sub':
        response.content_type = 'application/json'
        yield repr([reduce(operator.sub, ast.literal_eval(request.query.args))]).encode()
    elif operation == 'div':
        try:
            response.content_type = 'application/json'
            yield repr([reduce(operator.truediv, ast.literal_eval(request.query.args))]).encode()
        except ZeroDivisionError:
            response.status = 400
            yield "Пресечена попытка деления на ноль!".encode()


@get('/result')
@auth_basic(check)
def get_result():
    operation = request.query.operation
    args = request.query.args
    for result in results:
        if operation == result['operation'] and args == result['args']:
            response.content_type = 'application/json'
            return result['result']
    return "Еще не готово".encode()


def check_results(operation, args):
    for result in results:
        if operation == result['operation'] and args == result['args']:
            return True
    return False


def add_result(result):
    if result not in results:
        results.append(result)


@get('/slow/<operation>')
@auth_basic(check)
def slow_operation(operation):
    if operation == 'sqrt':
        threading.Thread(target=slow_sqrt, args=(ast.literal_eval(request.query.args),)).start()
        response.content_type = 'application/json'
        yield "Принято в обработку".encode()
    elif operation == 'fact':
        threading.Thread(target=slow_fact, args=(ast.literal_eval(request.query.args),)).start()
        response.content_type = 'application/json'
        yield "Принято в обработку".encode()


def slow_fact(args):
    if not check_results('fact', args):
        sleep(len(args) * 2)
        try:
            add_result({'operation': 'fact', 'args': f'{args}',
                        'result': repr(list(map(lambda x: math.factorial(x), args))).encode()})
        except ValueError:
            response.status = 400
            add_result({'operation': 'fact', 'args': f'{args}',
                        'result': "Пресечена попытка расчета факториала для отрицательного числа!".encode()})


def slow_sqrt(args):
    if not check_results('sqrt', args):
        sleep(len(args) * 2)
        add_result(
            {'operation': 'sqrt', 'args': f'{args}', 'result': repr(list(map(lambda x: x ** 0.5, args))).encode()})


@post('/login')
def login():
    credentials = request.auth
    if credentials is None:
        response.status = 401
        response.content_type = 'application/json'
        yield json.dumps({'success': False, 'message': 'Enter authentication data!'})
    elif check(*credentials):
        response.content_type = 'application/json'
        yield json.dumps({'success': True, 'message': f'Hello, {credentials[0]}!'})
    else:
        response.status = 404
        response.content_type = 'application/json'
        yield json.dumps({'success': False, 'message': "User with these credentials doesn't exist"})


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
    if not check_username(username):
        add_user(username, password)
        response.content_type = 'application/json'
        yield json.dumps({'success': True, 'message': 'Successful registration'})
    else:
        response.status = 403
        response.content_type = 'application/json'
        yield json.dumps({'success': False, 'message': 'Username already used'})


if __name__ == '__main__':
    monkey.patch_all()
    conn = sqlite3.connect('users.sqlite')
    cursor = conn.cursor()
    results = []
    cursor.execute("CREATE TABLE IF NOT EXISTS users (username TEXT, token TEXT);")
    run(host='localhost', port=8080, server='gevent')
    conn.close()
