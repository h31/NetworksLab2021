import ast
import base64
import math
import operator
import sqlite3
from functools import reduce
from time import sleep

from bottle import get, run, request, post, auth_basic, response


def check(username, password):
    token = base64.b64encode(bytes('%s:%s' % (username, password), 'utf-8')).decode('ascii')
    cursor.execute(
        f"SELECT COUNT(*) FROM users WHERE users.username = '{username}' AND users.token = '{token}';")
    return cursor.fetchone()[0]


@get('/fast/sum')
@auth_basic(check)
def fast_sum():
    return repr(reduce(operator.add, ast.literal_eval(request.query.args))).encode()


@get('/fast/mul')
@auth_basic(check)
def fast_mul():
    return repr(reduce(operator.mul, ast.literal_eval(request.query.args))).encode()


@get('/fast/sub')
@auth_basic(check)
def fast_sub():
    return repr(reduce(operator.sub, ast.literal_eval(request.query.args))).encode()


@get('/fast/div')
@auth_basic(check)
def fast_div():
    try:
        return repr(reduce(operator.truediv, ast.literal_eval(request.query.args))).encode()
    except ZeroDivisionError:
        response.status = 400
        return "Пресечена попытка деления на ноль!".encode()


@get('/slow/sqrt')
@auth_basic(check)
def slow_sqrt():
    sleep(10)
    return repr(list(map(lambda x: x ** 0.5, ast.literal_eval(request.query.args)))).encode()


@get('/slow/fact')
@auth_basic(check)
def slow_fact():
    sleep(10)
    try:
        return repr(list(map(lambda x: math.factorial(x), ast.literal_eval(request.query.args)))).encode()
    except ValueError:
        response.status = 400
        return "Пресечена попытка расчета факториала для отрицательного числа!".encode()


@post('/login')
def login():
    credentials = request.auth
    if credentials is None:
        response.status = 401
        return {'success': False,
                'message': 'Enter authentication data!'}
    elif check(*credentials):
        return {'success': True,
                'message': f'Hello, {credentials[0]}!'}
    else:
        response.status = 404
        return {'success': False,
                'message': 'User doesn\'t exist'}


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
        return {'success': True,
                'message': 'Successful registration'}
    else:
        response.status = 403
        return {'success': False,
                'message': 'Username already used'}


if __name__ == '__main__':
    conn = sqlite3.connect('users.sqlite')
    cursor = conn.cursor()
    cursor.execute("CREATE TABLE IF NOT EXISTS users (username TEXT, token TEXT);")
    run(host='localhost', port=8080)
    conn.close()
