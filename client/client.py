import requests


addr = "http://185.183.98.98:8080"

AUTHORIZATION = f'{addr}/api/authorization'
REGISTRATION = f'{addr}/api/registration'
PERSONAL_ACCOUNT = f'{addr}/api/lk'


def main():
    comm_list = ["login", "register", "logout", "count", "transfer"]
    print(comm_list)
    cookie = None
    while True:
        comm = input("Enter command: ")
        if comm not in comm_list:
            print("This command doesn't exist")
        else:
            if comm == "logout":
                cookie = None
                print("Successfully logged out")

            elif comm == "login":
                resp = requests.get(AUTHORIZATION, cookies=cookie)
                if resp.status_code == 200:
                    print(f"Already logged as {resp.json()['name']}")
                else:
                    login = input("Enter login: ")
                    pswd = input("Enter password: ")
                    resp = requests.post(AUTHORIZATION, json={'login': login, "pass": pswd})
                    if resp.status_code == 200:
                        cookie = resp.cookies
                        print("Successfully logged")
                    elif resp.status_code == 401:
                        print(resp.json()['ans'])

            elif comm == "register":
                resp = requests.get(REGISTRATION, cookies=cookie)
                if resp.status_code == 205:
                    print(resp.json()['ans'])
                else:
                    name = input("Enter a new user's name: ")
                    p = input("Enter a new user's password: ")
                    resp = requests.post(REGISTRATION, json={'login': name, 'pass': p}, cookies=cookie)
                    print(resp.json()['ans'])
                    cookie = resp.cookies

            elif comm == "count":
                resp = requests.get(PERSONAL_ACCOUNT, cookies=cookie)
                print(resp.json()['ans'])

            elif comm == "transfer":
                getter = input("Enter a getter's nickname: ")
                sum = int(input("Enter amount of money to transfer: "))
                resp = requests.post(PERSONAL_ACCOUNT, json={'username': getter, 'amount': sum},
                                     cookies=cookie)
                print(resp.json()['ans'])


if __name__ == "__main__":
    main()