import requests, sys
import html2text as html2text


def start():
	global session
	print(html2text.HTML2Text().handle(session.get(addr + "start").text))


def new_bet():
	global session
	amount = input("Enter amount (>= 1): ")
	print("Bet types: -2 (even), -1 (odd), 0-36 (specify number)")
	bet_type = input("Enter bet type: ")

	result = session.post(addr + "bet", {'amount': amount, 'type': bet_type})
	code = result.status_code
	match code:
		case 444:
			print("Incorrect type or amount")
		case 445:
			print("Insufficient number of coins")
		case _:
			print("Accepted")
	print()


def get_bet_list():
	global session
	print(html2text.HTML2Text().handle(session.get(addr + "bet/all").text))


def get_results():
	global session
	print(html2text.HTML2Text().handle(session.get(addr + "result").text))


def get_info():
	global session, croupier
	data = session.get(addr + "protected/json").json()
	print(f"You logged in as {data['login']}")
	if croupier == "on":
		print("You are a croupier (\"start\" command to spin the roulette wheel)")
	else:
		print(f"You have {data['coins']} coins")
	print()


if len(sys.argv) != 2:
	print("Arg: serverIP")
	exit()

addr = f"http://{sys.argv[1]}:5000/"

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
	case 441:
		print("You are already logged in")
		exit()
	case 442 | 443:
		print("Incorrect login or password")
		exit()
	case 435:
		print("The croupier already exists")
		exit()
	case 200 | 302:
		print()

while True:
	if croupier == "on":
		print("Commands: start / ls / results / info / logout")
	else: # off
		print("Commands: new / ls / results / info / logout")
	
	match input("Enter the command: "):
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
