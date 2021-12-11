# python_socketio==4.6.0; python_engineio==3.13.2; flask_socketio==4.3.1
from flask import Flask
from flask_socketio import SocketIO, emit, disconnect
import flask_login, functools, sys, math, socket
from flask_login import LoginManager, UserMixin, current_user
from multiprocessing import Pool

app = Flask(__name__)
app.secret_key = "secret_key"
socketio = SocketIO(app)

login_manager = LoginManager()
login_manager.init_app(app)
users = {"user1":"user1", "user2":"user2"}

@login_manager.user_loader
def user_loader(login):
	if login not in users:
		return

	user = UserMixin()
	user.id = login
	return user

@login_manager.request_loader
def request_loader(request):
	login = request.form.get("login")
	if login not in users:
		return

	user = UserMixin()
	user.id = login
	return user

@socketio.on('login')
def login(data):
	user_login = data["login"]
	if user_login in users:
		if data["password"] == users[user_login]:
			user = UserMixin()
			user.id = user_login
			flask_login.login_user(user)
			
	if current_user.is_authenticated:
		return {"status":"success", "user_login":current_user.get_id()}
	else:
		return {"status":"failed"}

def authenticated_only(f):
	@functools.wraps(f)
	def wrapped(*args, **kwargs):
		if not current_user.is_authenticated:
			disconnect()
		else:
			return f(*args, **kwargs)
	return wrapped

@socketio.on('fast_calc')
@authenticated_only
def fast_calc(data):	
	if data["operation"] not in ("+", "-", "*", "/"):
		return "ERROR: Wrong operation"
	try:
		operand1 = float(data["operand1"])
		operand2 = float(data["operand2"])
	except ValueError or TypeError:
		return "ERROR: Invalid operand"
	
	match data["operation"]:
		case "+":
			return "Answer: " + str(operand1) + " + " + str(operand2) + " = " + str(operand1 + operand2)
		case "-":
			return "Answer: " + str(operand1) + " - " + str(operand2) + " = " + str(operand1 - operand2)
		case "*":
			return "Answer: " + str(operand1) + " * " + str(operand2) + " = " + str(operand1 * operand2)
		case "/":
			if operand2 > -0.0000001 and operand2 < 0.0000001:
				return "ERROR: Division by zero"
			else:
				return "Answer: " + str(operand1) + " / " + str(operand2) + " = " + str(operand1 / operand2)

@socketio.on('slow_calc')
@authenticated_only
def slow_calc(data):
	if data["operation"] not in ("sqrt", "!"):
		return "ERROR: Wrong operation"
	try:
		value = float(data["value"])
	except ValueError or TypeError:
		return "ERROR: Invalid value"
	
	emit("server_message", "INFO: Please wait until the operation is completed")
	
	pool = Pool()
	asyncResult = pool.apply_async(calculate, (data["operation"], value))
	asyncResult.wait(int(sys.argv[1]))
	if asyncResult.ready():
		result = asyncResult.get()
		pool.terminate()
		return result
	else:
		pool.terminate()
		return "ERROR: Timeout"

def calculate(operation, value):
	if value < 0: return "ERROR: The value must be greater than 0"
	match operation:
		case "sqrt":
			return "Answer: sqrt(" + str(value) + ") = " + str(math.sqrt(value))
		case "!":
			try:
				factorial = math.factorial(int(value))
				return "Answer: " + str(int(value)) + "! = " + str(factorial)
			except OverflowError:
				return "ERROR: The factorial argument should not exceed 2147483647"

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
	