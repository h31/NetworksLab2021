import json

import requests
from prettytable import PrettyTable


class Client:

    def __init__(self):
        self.address = 'http://192.168.0.29:8080/'
        self.session = requests.Session()
        self.username = ""
        self.password = ""
        status = requests.codes.unauthorized
        while status != requests.codes.ok:
            try:
                login_method = input("1)\tLogin\n2)\tRegistration\n")
                self.set_username("Username:")
                self.set_password("Password:")
                if login_method == '1':
                    login = self.session.get(f'{self.address}login', auth=(self.username, self.password))
                    status = login.status_code
                    self.is_seller = False if json.loads(login.text)["message"] == "false" else True
                elif login_method == '2':
                    status = self.session.get(f'{self.address}register',
                                              data={'username': self.username, 'password': self.password}).status_code
                    self.is_seller = False
                else:
                    raise IndexError
            except IndexError:
                print("Incorrect option!")
                self.session.close()

    def set_username(self, reason=''):
        self.username = input(reason)

    def set_password(self, reason=''):
        self.password = input(reason)

    def show_markets(self):
        market_table = PrettyTable()
        market_table.field_names = ['№', 'Name', 'Area']
        markets = self.session.get(f'{self.address}markets', auth=(self.username, self.password)).json()
        prompt = ''
        ids = []
        for market in markets:
            ids.append(market['id'])
            prompt += f"{market['id']})\tShow {market['name']}'s goods\n"
            market_table.add_row([market['id'], market['name'], market['geoArea']])
        return prompt, ids, market_table

    def show_goods(self, id):
        market = self.session.get(f'{self.address}markets/{id}', auth=(self.username, self.password)).json()
        goods_table = PrettyTable()
        goods_table.field_names = ['№', 'Name', 'Price']
        print(f"{market['name']}'s goods")
        for market_goods in market['marketGoods']:
            goods_table.add_row([market_goods['goods']['id'], market_goods['goods']['name'], market_goods['price']])
        return goods_table

    def client_requests(self):
        while True:
            if not self.is_seller:
                option = input("1)\tList markets\n2)\tCreate order\n3)\tExit\n")
            else:
                option = input("1)\tList markets\n2)\tAdd market\n3)\tExit\n")
            if option == '1':
                prompt, ids, market_table = self.show_markets()
                print(market_table)
                prompt += '0)\tGo back\n'
                while option != 0:
                    option = int(input(prompt))
                    if option == 0:
                        continue
                    elif int(option) in ids:
                        print(self.show_goods(int(option)))
            elif option == '2':
                if not self.is_seller:
                    user_area = input("Enter your area: ")
                    _, ids, market_table = self.show_markets()
                    print(market_table)
                    market_id = int(input("Enter market №: "))
                    if market_id in ids:
                        print(self.show_goods(market_id))
                        goods_list = input("Enter goods №: ").split(" ")
                        body = {"marketId": str(market_id), "userArea": user_area, "goodsIdArray": goods_list}
                        prices = self.session.get(f'{self.address}order', data=json.dumps(body),
                                                  auth=(self.username, self.password)).json()
                        for entry in prices.items():
                            print(f"{entry[0]}\t{entry[1]}")
                        print(f"Total\t{sum(prices.values())}")
                else:
                    market_name = input("Enter market name: ")
                    market_area = int(input("Enter market area: "))
                    market_goods = []
                    while input('0)\tEnd\n1)\tAdd goods\n') != '0':
                        goods_name = input("Enter goods name: ")
                        goods_price = int(input("Enter goods price: "))
                        market_goods.append({'goodsName': goods_name, 'goodsPrice': goods_price})
                    market_data = {'marketName': market_name, 'marketArea': market_area, "goodsPriceList": market_goods}
                    self.session.post(f"{self.address}manage/add/market", data=json.dumps(market_data),
                                      auth=(self.username, self.password))
            elif option == '3':
                self.session.close()
                break
