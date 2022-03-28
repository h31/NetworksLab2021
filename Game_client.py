import requests
import json

if __name__ == '__main__':
    print("Hello")
    name = "login2"  # input("Name> ")
    address = "localhost:8080"  # input("Game server address> ")
    start = False
    n = None
    while True:
        action = input("> ")
        if action == "start":
            res = requests.post("http://" + address + "/start")
            if res.json():
                print("all bets")
                for bet in res.json():
                    print("name: ", bet["name"], ". type:", bet["type"], " amount:", bet["amount"], bet["status"])
            else:
                print("No bets")
        elif action == "bets":
            res = requests.get('http://' + address + "/bets")
            if res.json():
                for bet in res.json():
                    print("name: ", bet["name"], ". type:", bet["type"], " amount:", bet["amount"])
            else:
                print("No bets")
        elif action == "new_player":
            name = input("Name> ")
            balance = float(input("Balance> "))
            c = {"name": name, "balance": balance}
            res = requests.post('http://' + address + "/players", data=json.dumps(c))
        elif action == "players":
            res = requests.get('http://' + address + "/players").json()
            if res:
                for pl in res:
                    print("name: ", pl["name"], ". balance: ", pl["balance"])
            else:
                print("Empty desk")
        elif action == "croupier":
            res = requests.get('http://' + address + "/croupier").json()
            if res["name"]:
                print("name: ", res["name"], ". start: ", res["start"])
                if res["bets"]:
                    for bet in res["bets"]:
                        print("name: ", bet["name"], ". type:", bet["type"], " amount:", bet["amount"], bet["status"])
                else:
                    print("No bets")
            else:
                print("No croupier")
        elif action == "new_croupier":
            name = input("Name> ")
            c = {"name": name, "bets": [], "status": False}
            res = requests.post('http://' + address + "/croupier", data=json.dumps(c))
            if res.status_code != 200:
                print("error of request")
        elif action == "make_bets":
            name = input("Name> ")
            t = input("Type> ")
            amount = input("Amount> ")
            res = requests.post('http://' + address + "/bets/" + name + "/" + t + "/" + amount)
            if res.status_code != 200:
                print("error of request")
        elif action == "get_prize":
            name = input("Name> ")
            balance = input("Balance> ")
            res = requests.post('http://' + address + "/players/" + name + "/" + balance)
            if res.status_code != 200:
                print("error of request")
        elif action == "quit":
            break
        else:
            print("""
Incorrect command

List of command:
    start - start round
    bets - get all bets
    new_player - make new player
    players - get all players
    croupier - get croupier info
    new_croupier - set croupier to work table
    make_bets - make new bet
    get_prize - get your prize
    quit
            """)
    print("Bye")
