import sys, re, requests


def parse(string):
    string = string.replace(" ", "")
    if re.fullmatch("([0-9]+[.|])?[0-9]+\+([0-9]+[.|])?[0-9]+", string):
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


if len(sys.argv) == 4:
    server = "http://" + sys.argv[1] + ":5000/"
    login = sys.argv[2]
    password = sys.argv[3]
    try:
        # r = requests.get("http://192.168.0.104:5000/", auth = (login, password))
        r = requests.get(server, auth=(login, password))
    except requests.exceptions.RequestException:
        print("The server is unavailable")
        exit()

    if r.text != "Hello. You are logged in.":
        print("Invalid login or password")
        exit()
else:
    print("Args: login password")
    exit()

print("Fast operations: 1+2, 2-3, 3*4, 4/5")
print("Slow operations: sqrt(25), 5!")
print("Enter \q to quit", end="\n\n")

fast_operations = ["+", "-", "*", "/"]
slow_operations = ["sqrt", "!"]
server += "calc"
while True:
    string = input("Enter the expression: ")
    if string == "\q": break
    operation, operand1, operand2 = parse(string)
    if operation == None:
        print("Wrong operation")
        continue

    try:
        if operation in fast_operations:
            r = requests.get(server,
                             params={"operation": operation, "operand1": operand1, "operand2": operand2},
                             auth=(login, password))
        else:
            r = requests.get(server,
                             params={"operation": operation, "value": operand1},
                             auth=(login, password))
    except requests.exceptions.RequestException:
        print("The server is unavailable")
        break

    print("Answer: " + r.text, end="\n\n")
