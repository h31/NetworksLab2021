import os
import json
import sqlite3
from pprint import pprint

from bottle import route, run, request, response



# Struct of db and main routine:
# users:
#
# userId
#   contains id for check, who buy, who increase/decrease price
# group
#   100 - simple user
#   10  - administrator
# -----------------
# items:
#
# itemId
#   contains itemId for check price
# itemPrice
#   contains itemPrice

def main():
    checkDb()
    run(host='localhost', port=10000)

@route('/createAccount', method='POST')
def createAccount():
    result = json.loads(request.body.getvalue().decode('utf-8'))
    userGroup = result["userGroup"]
    userName = result["userName"]
    userPassword = result["userPassword"]
    query = "INSERT INTO users (userGroup, userName, userPassword) VALUES ('%s', '%s', '%s');"%(userGroup,userName,userPassword)
    dbCur.execute(query)
    dbConnection.commit()
    response.status = 200
    return 'Выполнено'

@route('/removeAccount', method='POST')
def removeAccount():
    result = json.loads(request.body.getvalue().decode('utf-8'))
    userId = result['userId']
    query = "delete from users where userId='%s'"%userId
    dbCur.execute(query)
    dbConnection.commit()
    response.status = 200
    return 'Выполнено'

@route('/printUser', method='GET')
def printUsers():
    result = json.loads(request.body.getvalue().decode('utf-8'))
    currentUserId = result["userId"]
    query = "select * from users where userId='%s'" % currentUserId
    dbCur.execute(query)
    currentUser = dbCur.fetchall()
    if len(currentUser)==0 or currentUser[0][1] != '10':
        response.status = 400
        return "Operation not allowed!"
    else:
        response.status = 200
        query = 'select * from users'
        dbCur.execute(query)
        return(json.dumps(dbCur.fetchall()))


@route('/logIn',method='POST')
def logIn():
    result = json.loads(request.body.getvalue().decode('utf-8'))
    userName = result['userName']
    userPassword = result['userPassword']
    query = "select * from users where userName='%s' and userPassword='%s'"%(userName,userPassword)
    dbCur.execute(query)
    userInfo = dbCur.fetchall()
    if userPassword != userInfo[0][3]:
        response.status = 401
        return "Wrong username or password"
    else:
        response.status = 200
        userFullInfo = {
            'userId': userInfo[0][0],
            'userGroup': userInfo[0][1]
        }
        return (json.dumps(userFullInfo))



@route('/changePrice', method='POST')
def changePrice():
    # Execute when user increase price
    result = json.loads(request.body.getvalue())
    id = result['itemId']
    summ = result['itemPrice']
    query = "select * from items where itemId=%s"%id
    dbCur.execute(query)
    itemPrice = dbCur.fetchone()[2]
    newPrice = itemPrice + summ;
    query = "update items set itemPrice='%s' where itemId='%s'"%(newPrice,id)
    dbCur.execute(query)
    dbConnection.commit()
    response.status = 200
    return 'Выполнено'


@route('/printItems', method='GET')
def printItems():
    query = "select * from items"
    dbCur.execute(query)
    return(json.dumps(dbCur.fetchall()))

def exitApp():
    dbConnection.close()
# Clear sockets on exit

@route('/addItem', method='POST')
def addItem():
    # Execute when admin add item to exchange
    result = json.loads(request.body.getvalue().decode('utf-8'))
    name = result['name']
    price = result['price']
    query = "insert into items (itemName, itemPrice) values ('%s', '%s')"%(name,price)
    dbCur.execute(query)
    dbConnection.commit()
    response.status = 200
    return 'Выполнено'

@route('/removeItem', method='POST')
def removeItem():
    # Execute when admin remove item from exchange
    result = json.loads(request.body.getvalue().decode('utf-8'))
    id = result['id']
    query = "delete from items where itemId='%s'"%id
    dbCur.execute(query)
    dbConnection.commit()
    response.status = 200
    return 'Выполнено'

def checkDb():
    # Execute on start, check if db exist and contains tables
    # If db exist nothing happens
    # If db dont exist create tables and add default data
    query = "select exchangeVersion from info"
    try:
        dbCur.execute(query)
    except sqlite3.OperationalError:
        print(Warning("Cannot find database or database in invalid format! Create database."))
        createTables()
    else:
        dbVersion = dbCur.fetchall()
        print("Find database.")
        print("Database version %s" % dbVersion)
        return


def createTables():
    ### Create tables
    query = '''create table items
                (
                itemId integer
                constraint items_pk
                primary key,
                itemName text,
                itemPrice integer
                );'''
    dbCur.execute(query)

    query = '''create table users
                    (
                    userId integer 
                    constraint items_pk
                    primary key,
                    userGroup text,
                    userName text,
                    userPassword text
                    );'''

    dbCur.execute(query)
    query = '''create table info
                    (
                    exchangeVersion text
                    );'''
    dbCur.execute(query)
    dbConnection.commit()
    query = '''insert into info (exchangeVersion) values ('v0.1');'''
    dbCur.execute(query)
    dbConnection.commit()
    query = '''insert into users (userGroup, userName, userPassword) values ('100','user1', '12345678') '''
    dbCur.execute(query)
    dbConnection.commit()
    query = '''insert into users (userGroup, userName, userPassword) values ('10', 'admin', '12345678') '''
    dbCur.execute(query)
    dbConnection.commit()
    ### Insert db version
    ### Mb apply version from file


if __name__ == "__main__":
    HOST = 'http://localhost:10000'
    dbConnection = sqlite3.connect('exchange.db')
    dbCur = dbConnection.cursor()
    main()
