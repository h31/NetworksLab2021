# python_socketio==4.6.0; python_engineio==3.13.2; flask_socketio==4.3.1
from socketIO_client import SocketIO
import sys, re

def parse(string):
	string = string.replace(" ", "")
	if re.fullmatch("(-|)([0-9]+[.|])?[0-9]+\+([0-9]+[.|])?[0-9]+", string):
		operator = "+"
		operands = string.split("+")
	elif re.fullmatch("([0-9]+[.|])?[0-9]+\-([0-9]+[.|])?[0-9]+", string):
		operator = "-"
		operands = string.split("-")
	elif re.fullmatch("([0-9]+[.|])?[0-9]+\*([0-9]+[.|])?[0-9]+", string):
		operator = "*"
		operands = string.split("*")
	elif re.fullmatch("([0-9]+[.|])?[0-9]+\/([0-9]+[.|])?[0-9]+", string):
		operator = "/"
		operands = string.split("/")
	elif re.fullmatch("sqrt\(([0-9]+[.|])?[0-9]+\)", string):
		operator = "sqrt"
		operands = (string[5:-1], None)
	elif re.fullmatch("\d+!", string):
		operator = "!"
		operands = (string[:-1], None)
	else:
		return None, None, None

	return operator, operands[0], operands[1]

if len(sys.argv) != 4:
	print("Args: server login password")
	exit()

sio = SocketIO("http://" + sys.argv[1], 5000)

def show_callback_result(*args):
	print(*args)
	print()
sio.on("server_message", show_callback_result)

# Step 3 = make requests for calculations and get the results
def input_handler():
	while True:
		string = input()
		if string == "\q": break
		operation, operand1, operand2 = parse(string)
		if operation == None:
			print("Wrong operation")
			continue
	
		if operation in ("+", "-", "*", "/"):
			sio.emit(	'fast_calc', 
						{"operation":operation, "operand1":operand1, "operand2":operand2},
						callback = show_callback_result
			)
		if operation in ("sqrt", "!"):
			sio.emit(	'slow_calc',
						{"operation":operation, "value":operand1},
						callback = show_callback_result
			)
		sio.wait_for_callbacks()

# Step 2 = successful / failed login
def login_status(*data):
	if data[0]["status"] != "success":
		print("Invalid login or password")
	else:
		print("Logged in as", data[0]["user_login"])
		print("Fast operations: 1+2, 2-3, 3*4, 4/5")
		print("Slow operations: sqrt(25), 5!")
		print("Enter \q to quit")
		print()
		input_handler()

# Step 1 = try to login
sio.emit(	"login",
			{'login':sys.argv[2], 'password':sys.argv[3]},
			callback = login_status
)
sio.wait_for_callbacks()

# before exit
sio.disconnect()