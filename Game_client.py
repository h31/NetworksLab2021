import requests
import json

if __name__ == '__main__':
    print("Hello")
    name = "login2"  # input("Name> ")
    address = "localhost:8080"  # input("Game server address> ")
    start = False
    n = None
    c = {}
    isPlayer = False
    if input("Hello! choose your role!) player/croupier: ") == "player":
        isPlayer = True
    while True:
        action = input("> ")
        if action == "start":
            res = requests.post("http://" + address + "/start")
            if res.json():
                print("all bets")
                for bet in res.json():
                    print("player: ", bet["name"], ". type:", bet["type"], " amount:", bet["amount"], bet["status"])
            else:
                print("No bets")
                
        elif action == "bets":
            res = requests.get('http://' + address + "/bets")
            if res.json():
                for bet in res.json():
                    print("player: ", bet["name"], ". type:", bet["type"], " amount:", bet["amount"])
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
                    print("player: ", pl["name"], ". balance: ", pl["balance"])
            else:
                print("Empty desk")
                
        elif action == "croupier" and isPlayer:
            res = requests.get('http://' + address + "/croupier").json()
            if res["name"]:
                print("croupier: ", res["name"], ". start: ", res["start"])
                if res["bets"]:
                    for bet in res["bets"]:
                        print("player: ", bet["name"], ". type:", bet["type"], " amount:", bet["amount"], bet["status"])
                else:
                    print("No bets")
            else:
                print("No croupier")
                
        elif action == "new_croupier" and not isPlayer:
            name = input("Name> ")
            croup = requests.get('http://' + address + "/croupier").json()
            if croup and name == croup["name"]:
                print("Welcome back!")
            elif not croup["name"]:
                cr = {"name": name, "bets": [], "status": False}
                res = requests.post('http://' + address + "/croupier", data=json.dumps(cr))
                if res.status_code != 200:
                    print("error of request")
            else:
                print("Croupier already exist. You're player.")
                isPlayer = True
                
        elif action == "make_bets" and isPlayer:
            t = input("Type> ")
            amount = input("Amount> ")
            res = requests.post('http://' + address + "/bets/" + name + "/" + t + "/" + amount)
            if res.status_code != 200:
                print("error of request")
                
        elif action == "get_prize" and isPlayer:
            name = input("Name> ")
            croup = requests.get('http://' + address + "/croupier").json()
            if croup and name == croup["name"]:
                print("Welcome back!")
            elif not croup["name"]:
                cr = {"name": name, "bets": [], "status": False}
                res = requests.post('http://' + address + "/croupier", data=json.dumps(cr))
                if res.status_code != 200:
                    print("error of request")
            else:
                print("Croupier already exist. You're player.")
                isPlayer = True
        elif action == "quit":
            break
        elif isPlayer:
            print("""
Incorrect command

List of command for player:
    start - start round
    bets - get all bets
    new_player - make new player
    players - get all players
    croupier - get croupier info
    make_bets - make new bet
    get_prize - get your prize
    quit
            """)
        else:
            print("""
Incorrect command

List of command for crupier:
    start - start round
    bets - get all bets
    players - get all players
    new_croupier - set croupier to work table
    quit
            """)
    print("Bye")
