# python_socketio==4.6.0; python_engineio==3.13.2; flask_socketio==4.3.1
from flask import Flask
from flask_socketio import SocketIO, emit
from flask_login import LoginManager, UserMixin, current_user, login_user, logout_user
from multiprocessing import Pool
import sys, math, socket

app = Flask(__name__)
app.secret_key = "secret_key"
socketio = SocketIO(app)

login_manager = LoginManager()
login_manager.init_app(app)
users = {"user1":"user1", "user2":"user2", "user3":"user3", "user4":"user4", "user5":"user5"}
logged_in_users = []

@login_manager.user_loader
def user_loader(login):
	if login not in logged_in_users:
		return

	user = UserMixin()
	user.id = login
	return user

@socketio.on('login')
def login(data):
	user_login = data["login"]
	if user_login in users:
		if data["password"] == users[user_login]:
			if user_login not in logged_in_users:
				logged_in_users.append(user_login)
			user = UserMixin()
			user.id = user_login
			login_user(user)
			
	if current_user.is_authenticated:
		return {"status":"success", "user_login":current_user.get_id()}, 200
	else:
		return {"status":"failed"}, 401

@socketio.on("logout")
def logout():
	if current_user.is_authenticated:
		logged_in_users.remove(current_user.get_id())
		logout_user()

@socketio.on("disconnect")
def disconnect():
	logout()

@socketio.on('fast_calc')
def fast_calc(data):	
	if not current_user.is_authenticated:
		return {"ERROR":"Login required"}, 400
	
	if data["operation"] not in ("+", "-", "*", "/"):
		return {"ERROR":"Wrong operation"}, 400
	try:
		operand1 = float(data["operand1"])
		operand2 = float(data["operand2"])
	except ValueError or TypeError:
		return {"ERROR":"Invalid operand"}, 400
	
	match data["operation"]:
		case "+":
			return {"Answer":str(operand1) + " + " + str(operand2) + " = " + str(operand1 + operand2)}, 200
		case "-":
			return {"Answer":str(operand1) + " - " + str(operand2) + " = " + str(operand1 - operand2)}, 200
		case "*":
			return {"Answer":str(operand1) + " * " + str(operand2) + " = " + str(operand1 * operand2)}, 200
		case "/":
			if operand2 > -0.0000001 and operand2 < 0.0000001:
				return {"ERROR":"Division by zero"}, 400
			else:
				return {"Answer":str(operand1) + " / " + str(operand2) + " = " + str(operand1 / operand2)}, 200

@socketio.on('slow_calc')
def slow_calc(data):
	if data["operation"] not in ("sqrt", "!"):
		return {"ERROR":"Wrong operation"}, 400
	try:
		value = float(data["value"])
	except ValueError or TypeError:
		return {"ERROR":"Invalid value"}, 400
	
	emit("server_message", ({"INFO":"Please wait until the operation is completed"}, 200))
	
	global timeout
	pool = Pool()
	asyncResult = pool.apply_async(calculate, (data["operation"], value))
	asyncResult.wait(timeout) # waiting for a timeout or until the result is available
	if asyncResult.ready():
		result = asyncResult.get()
		pool.terminate()
		return result
	else:
		pool.terminate()
		return {"ERROR":"Timeout"}, 400

def calculate(operation, value):
	if value < 0: return {"ERROR":"The value must be greater than 0"}, 400
	match operation:
		case "sqrt":
			return {"Answer":"sqrt(" + str(value) + ") = " + str(math.sqrt(value))}, 200
		case "!":
			try:
				factorial = math.factorial(int(value))
				return {"Answer":str(int(value)) + "! = " + str(factorial)}, 200
			except OverflowError:
				return {"ERROR":"The factorial argument should not exceed 2147483647"}, 400

def get_local_IP():
	sock = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
	sock.connect(("8.8.8.8", 80))
	ip = sock.getsockname()[0]
	sock.close()
	return ip

if __name__ == "__main__":
	global timeout
	match len(sys.argv):
		case 1:
			timeout = 2 # default timeout
		case 2:
			if sys.argv[1].isdigit():
				timeout = int(sys.argv[1])
			else:
				print("Arg: timeout")
				exit()
		case _:
			print("Arg: timeout")
			exit()
	
	app.run(host = get_local_IP(), port = 5000)
