import json
import requests
from bottle import request

import client

HOST = 'http://localhost:10000'
LOGGED = False

userInfo = {
    'userId': '',
    'userGroup': '',
    'userName': '',
    'userPassword': ''
}


def main():
    help()
    while True:
        userInput = input()
        match userInput.strip(' '):
            case 'help':
                help()
            case 'logIn':
                loginIn()
            case 'printUsers':
                printUsers()
            case 'printItems':
                printItems()
            case 'addItem':
                addItem()
            case 'removeItem':
                removeItem()
            case 'changePrice':
                changePrice()
            case 'createAccount':
                createAccount()
            case 'removeAccount':
                removeAccount()


def help():
    stroke =\
    '''
    Введите команду с клавиатуры:\n
    help            - отобразить эту подсказку\n
    logIn           - зайти в систему\n
    printUsers      - вывести список пользователей\n
    printItems      - вывести список торгов\n
    addItem         - добавить запись в систему\n
    removeItem      - удалить запись из системы\n
    changePrice     - изменить цену на указанное кол-во\n
    removeAccount   - удалить пользователя
    '''
    print(stroke)



def loginIn():
    print('Введите имя пользователя:')
    userInfo['userName'] = input()
    print('Введите пароль:')
    userInfo['userPassword'] = input()
    result = requests.post(HOST + '/logIn', data=json.dumps(userInfo))
    match result.status_code:
        case 200:
            userInfo['userId'] = result.json()['userId']
            userInfo['userGroup'] = result.json()['userGroup']
            client.LOGGED = True
            print(result.text)
            return
        case 401:
            print(result.text)
            return


def printUsers():
    if client.LOGGED != True:
        print("Вы не вошли в систему")
        return
    req = requests.get(HOST + '/printUser', data=json.dumps(userInfo))
    match req.status_code:
        case 200:
                print(req.json())
        case 400:
            print(req.text)
            return


def printItems():
    if client.LOGGED != True:
        print("Вы не вошли в систему")
        return
    print("Текущие торги:")
    req = requests.get(HOST + '/printItems')
    print(req.json())
    return

def addItem():
    if userInfo["userGroup"] != '10':
        print("Not Allowed")
        return
    print("Введите название торга:")
    name = input()
    print("Введите начальную цену")
    price = input()
    item = {
        'name': name,
        'price': price
        }
    req = requests.post(HOST + '/addItem', data=json.dumps(item) )
    printItems()

def removeItem():
    if userInfo["userGroup"] != '10':
        print("Not Allowed")
        return
    printItems()
    print("Введите id:")
    id = input()
    item = {
        'id': id
    }
    req = requests.post(HOST + '/removeItem', data=json.dumps(item))
    print()
    return req.text

def changePrice():
    if client.LOGGED != True:
        print("Вы не вошли в систему")
        return
    printItems()
    print("Введите id:")
    itemId = input()
    print("Введите насколько хотите повысить цену")
    itemPrice = int(input())
    item = {
        'itemId': itemId,
        'itemPrice': itemPrice
    }
    if itemPrice <= 0:
        print("Нельзя снижать цену ")
        return
    req = requests.post(HOST + '/changePrice', data=json.dumps(item))
    print(req.text)
    printItems()
    return

def createAccount():
    printUsers()
    if userInfo["userGroup"] != '10':
        print("Not Allowed")
        return
    print("Введите уровень прав пользователя "
          "(10 - администратор,"
          " 100 - пользователь")
    userGroup = input();
    print("Введите имя пользователя:")
    userName = input()
    print("Введите пароль пользователя:")
    userPassword = input()
    newUser = {
        'userGroup': userGroup,
        'userName': userName,
        'userPassword': userPassword
    }
    req = requests.post(HOST + '/createAccount', data=json.dumps(newUser))
    printUsers()

def removeAccount():
    printUsers()
    if userInfo["userGroup"] != '10':
        print("Not Allowed")
        return
    print("Введите id пользователя для удаления:")
    userId = int(input())
    user = {
        'userId': userId
    }
    req = requests.post(HOST + '/removeAccount', data=json.dumps(user))
    printUsers()


if __name__ == '__main__':

    main()
