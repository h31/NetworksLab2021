from flask import Flask, request
import sys, math, socket
from multiprocessing import Pool
from flask_basicauth import BasicAuth

app = Flask("app")
app.config['BASIC_AUTH_USERNAME'] = 'login'
app.config['BASIC_AUTH_PASSWORD'] = 'password'
app.config['BASIC_AUTH_FORCE'] = True
basic_auth = BasicAuth(app)

fast_operations = ["+", "-", "*", "/"]
slow_operations = ["sqrt", "!"]

@app.route('/', methods=["GET"])
def login():
	return("Hello. You are logged in.")

@app.route('/calc', methods=["GET"])
def getResult():
	operation = request.args.get("operation")
	if operation in fast_operations:
		try:
			operand1 = float(request.args.get("operand1"))
			operand2 = float(request.args.get("operand2"))
		except ValueError or TypeError:
			return "ERROR: Invalid operand"
		
		return str(calc_fast_operation(operation, operand1, operand2))
	elif operation in slow_operations:
		try:
			value = float(request.args.get("value"))
		except ValueError or TypeError:
			return "ERROR: Invalid value"
		
		pool = Pool()
		asyncResult = pool.apply_async(calc_slow_operation, (operation, value))
		asyncResult.wait(int(sys.argv[1]))
		if asyncResult.ready():
			answer = asyncResult.get()
			pool.terminate()
			return answer
		else:
			pool.terminate()
			return "ERROR: Time out"
	else:
		return "ERROR: Wrong operation"

def calc_fast_operation(operation, operand1, operand2):
	match operation:
		case "+":
			answer = str(operand1) + " + " + str(operand2) + " = " + str(operand1 + operand2) 
		case "-":
			answer = str(operand1) + " - " + str(operand2) + " = " + str(operand1 - operand2) 
		case "*":
			answer = str(operand1) + " * " + str(operand2) + " = " + str(operand1 * operand2) 
		case "/":
			if operand2 > -0.0000001 and operand2 < 0.0000001:
				answer = "ERROR: Division by zero"
			else:
				answer = str(operand1) + " / " + str(operand2) + " = " + str(operand1 / operand2) 
	return answer

def calc_slow_operation(operation, value):
	if value < 0: return "The value must be greater than 0"
	match operation:
		case "sqrt":
			answer = "sqrt(" + str(value) + ") = " + str(math.sqrt(value))
		case "!":
			answer =  str(int(value)) + "! = " + str(math.factorial(int(value)))
	return answer

def get_local_IP():
	sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	sock.connect(("8.8.8.8", 80))
	ip = sock.getsockname()[0]
	sock.close()
	return ip

if __name__ == "__main__":
	if len(sys.argv) != 2 or not sys.argv[1].isdigit():
		print("Args: timeout")
		exit()
	app.run(host = get_local_IP(), port = 5000)
	