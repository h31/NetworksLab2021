import json
import requests

HOST ='http://localhost:4567'

def addProduct():
    data = {}
    login = "admin"
    password = input('Введите пароль:')
    name = input('Введите название продукта:')
    price = input('Введите цену продукта:')
    count = input('Введите количество продукта:')
    data['name'] = name
    data['price'] = price
    data['count'] = count
    response = requests.post(HOST + '/product', data=json.dumps(data), auth=(login, password))
    if response.status_code == 200:
        print("Вы успешно добавили продукт!")
    else:
        print(response.text)


def increaseProduct():
    data={}
    login = "admin"
    password = input('Введите пароль администратора:')
    id = input("Введите идентификатор продукта:")
    count = input("Введите количество продукта:")
    data['add'] = count
    response = requests.put(HOST + '/product/' + id, data=json.dumps(data), auth=(login, password))
    if response.status_code == 200:
        print("Количество продукта увеличено")
    else:
        print(response.text)


def getProductData():
    id = input("Введите идентификатор продукта:")
    response = requests.get(HOST + '/product/' + id)
    product = json.loads(response.text)
    if product["status"] == "success":
        print("Продукт" + str(product["data"]))
    else:
        print(response.text)


def order():
    data={}
    id = input("Введите идентификатор продукта:")
    count = input("Введите количество продукта:")
    data['id'] = id
    data['count'] = count
    response = requests.post(HOST + '/order', data=json.dumps(data))
    product = json.loads(response.text)
    if product["status"] == "success":
        print(str(product["data"]))
    else:
        print(response.text)


def service():
    while True:
        command = input("Введите команду: ")
        if command.strip(' ') == 'add product':
            addProduct()
        if command.strip(' ') == 'order':
            order()
        if command.strip(' ') == 'increase product':
            increaseProduct()
        if command.strip(' ') == 'get product data':
            getProductData()

if __name__ == '__main__':
    service()