import ast
import base64
import http
import json
import math
import operator
from functools import reduce
from http.server import BaseHTTPRequestHandler


class HttpProcessor(BaseHTTPRequestHandler):
    def do_GET(self):
        keys = map(lambda x: 'Basic ' + str(x), self.server.get_auth_key())

        if self.headers.get('Authorization') is None:
            self.do_AUTHHEAD()

            response = {
                'success': False,
                'error': 'No auth header received'
            }

            self.wfile.write(bytes(json.dumps(response), 'utf-8'))

        elif self.headers.get('Authorization') in keys:
            self.send_response(200)
            self.send_header('content-type', 'text/html')
            self.end_headers()

            content_length = int(self.headers['Content-Length'])
            rtype = self.requestline.split()[1].split('/')
            body = self.rfile.read(content_length)
            operands = ast.literal_eval(body.decode())

            if 'SUM' in rtype:
                self.wfile.write(repr(reduce(operator.add, operands)).encode())
            elif 'MUL' in rtype:
                self.wfile.write(repr(reduce(operator.mul, operands)).encode())
            elif 'SUB' in rtype:
                self.wfile.write(repr(reduce(operator.sub, operands)).encode())
            elif 'DIV' in rtype:
                self.wfile.write(repr(reduce(operator.truediv, operands)).encode())
            elif 'SQRT' in rtype:
                self.wfile.write(repr(list(map(lambda x: x ** 0.5, operands))).encode())
            elif 'FACTORIAL' in rtype:
                try:
                    self.wfile.write(repr(list(map(lambda x: math.factorial(x), operands))).encode())
                except ValueError:
                    self.wfile.write("Пресечена попытка расчета факториала для отрицательного числа!".encode())

        else:
            self.do_AUTHHEAD()

            response = {
                'success': False,
                'error': 'Incorrect credentials'
            }

            self.wfile.write(bytes(json.dumps(response), 'utf-8'))

    def do_POST(self):
        if self.headers.get('Authorization') is None:
            self.do_AUTHHEAD()

            response = {
                'success': False,
                'error': 'No auth header received'
            }

            self.wfile.write(bytes(json.dumps(response), 'utf-8'))

        else:
            rtype = self.requestline.split()[1].split('/')[1]

            if 'REGISTRATION' in rtype:
                username, password = map(lambda x: x.split('=')[1],
                                         self.requestline.split()[1].split('/')[1].split('?')[1].split('&'))
                if username not in self.server.get_users():
                    self.send_response(200)
                    self.send_header('content-type', 'text/html')
                    self.end_headers()
                    self.server.add_auth(username, password)
                    response = {
                        'success': True,
                        'message': 'Successful registration'
                    }
                    self.wfile.write(bytes(json.dumps(response), 'utf-8'))
                else:
                    response = {
                        'success': False,
                        'message': 'Username already used'
                    }
                    self.wfile.write(bytes(json.dumps(response), 'utf-8'))


    def do_AUTHHEAD(self):
        self.send_response(401)
        self.send_header('WWW-Authenticate', 'Basic realm=""')
        self.send_header('Content-type', 'application/json')
        self.end_headers()


class CustomHTTPServer(http.server.HTTPServer):
    key = []
    unames = []

    def __init__(self, address, handlerClass=HttpProcessor):
        super().__init__(address, handlerClass)

    def add_auth(self, username, password):
        self.unames.append(username)
        self.key.append(base64.b64encode(bytes('%s:%s' % (username, password), 'utf-8')).decode('ascii'))

    def get_auth_key(self):
        return self.key

    def get_users(self):
        return self.unames


if __name__ == '__main__':
    serv = CustomHTTPServer(('localhost', 80))
    serv.add_auth('admin', 'admin')
    serv.serve_forever()
