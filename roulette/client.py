import requests


def start():
	global session
	response = session.get(addr + "start")
	match response.status_code:
		case 200:
			print(response.text)
		case _:
			print("Server error")
			exit()


def new_bet():
	global session
	amount = input("Amount (>= 1): ")
	print("Bet types: -2 (even), -1 (odd), 0-36 (specify number)")
	bet_type = input("Bet type: ")

	result = session.post(addr + "bet", {'amount': amount, 'type': bet_type})
	code = result.status_code
	match code:
		case 200:
			print("Accepted")
		case 444:
			print("Incorrect type or amount")
		case 445:
			print("Insufficient number of coins")
		case _:
			print("Server error")
			exit()


def get_bet_list():
	global session
	response = session.get(addr + "bet/all/json")
	match response.status_code:
		case 200:
			data = response.json()
			for i in range(len(data["users"])):
				match data["types"][i]:
					case -2:
						t = "even"
					case -1:
						t = "odd"
					case _:
						t = str(data["types"][i])
				
				print("User: " + data["users"][i] + ", amount: " + 
					str(data["amounts"][i]) + ", type: " + t)
		case 204:
			print("Bets not found")
		case _:
			print("Server error")


def get_results():
	global session
	response = session.get(addr + "result/json")
	match response.status_code:
		case 200:
			data = response.json()
			print("The result of the roulette draw:", data["number"])
			for i in range(len(data["users"])):
				match data["types"][i]:
					case -2:
						t = "even"
					case -1:
						t = "odd"
					case _:
						t = str(data["types"][i])
				print("User: " + data["users"][i] + ", amount: " + str(data["amounts"][i]) + 
					", type: " + t +  ", RESULT: " + data["result"][i])
		case 204:
			print("Results not found")
		case _:
			print("Server error")


def get_info():
	global session, croupier
	data = session.get(addr + "userInfo/json").json()
	
	print(f"You logged in as {data['login']}")
	
	if croupier == "on":
		print("You are a croupier (\"start\" command to spin the roulette wheel)")
	else:
		print(f"You have {data['coins']} coins")


addr = input("Server (example: http://192.168.0.100:5000/): ")
addr = "http://192.168.0.101:5000/" # TMP
login = input("Login: ")
password = input("Password: ")
match input("Login as croupier? [yes/no]: "):
	case "yes" | "y":
		croupier = "on"
	case "no" | "n":
		croupier = "off"
	case _:
		print("Wrong answer")
		exit()

session = requests.Session()
result = session.post(addr + "login", {'login': login, 'password': password, 'croupier': croupier})
match result.status_code:
	case 200:
		print("Login successful")
	case 441:
		print("You are already logged in")
		exit()
	case 442 | 443:
		print("Incorrect login or password")
		exit()
	case 435:
		print("The croupier already exists")
		exit()
	case _:
		print("Server error")
		exit()

while True:
	print()
	if croupier == "on":
		print("Commands: start / ls / results / info / logout")
	else: # off
		print("Commands: new / ls / results / info / logout")
	
	match input(">> "):
		case "start":
			if croupier == "on":
				start()
			else:
				print("Wrong command")
		case "new":
			if croupier == "off":
				new_bet()
			else:
				print("Wrong command")
		case "ls":
			get_bet_list()
		case "results":
			get_results()
		case "info":
			get_info()
		case "logout":
			break
		case _:
			print("Wrong command")

session.get(addr + "logout")
