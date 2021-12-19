# Roulette API
## Errors
ERROR 435 - The croupier already exists
ERROR 441 - try login twice
ERROR 442 - Some field is not filled
ERROR 443 - Incorrect login or password
ERROR 444 - Incorrect type or amount
ERROR 445 - insufficient number of coins

## Routes
- POST /login
  - WWW-FORM{"login": "String", "password": "String", "croupier": "String"}
  - Ответы:
    - Code: 441:
      - You already login
    - Code: 443 | 442:
      - Incorrect login or password
    - Code: 435:
      - The croupier already exists
    - Code: 302 | 200:
      - Login successful
- GET /login
  - Ответы:
    - Code:200
      - text/html
- GET /userInfo
  - Ответы:
    - Code:200
      - text/html
    - Code:401
      - Unauthorized
- GET /userInfo/json
  - Ответы:
    - Code:200
      - JSON{"coins": "Int", "is_croupier": "boolean", "login": "String"}
    - Code:401
      - Unauthorized
- GET /logout
  - Ответы:
    - Code:200
      - html/text
    - Code:401
      - Unauthorized
- GET /bet
  - Ответы:
    - Code: 200
      - html/text
    - Code: 401
      - Unauthorized
    - Code: 403
      - You are croupier. You can't do bets
- POST /bet
  - Ответы:
    - Code: 200
      - bet accepted
    - Code: 401
      - Unauthorized
    - Code: 403
      - You are croupier. You can't do bets
    - Code: 444
      - Incorrect type or amount
    - Code: 445
      - Insufficient number of coins
- GET /bet/all
  - Ответы:
    - Code: 200
      - html/text
- GET /bet/all/json
  - Ответы:
    - Code: 200
      - JSON:{"users": list<String>,"types": list<Int>,"amounts": list<Int>}
    - Code: 204
      - Bets not found
- GET /start
  - Ответы:
    - Code: 200
      - html/text
    - Code: 401
      - Unauthorized
    - Code: 403
      - You must be croupier
- GET /result
  - Ответы:
    - Code: 200
      - html/text
- GET /result/json
  - Ответы:
    - Code: 204
      - No results yet
    - Code: 200
      - JSON:{"number": Int, "users": list<String>,"types": list<Int>,"amounts": list<Int>, "result": list<String>}

## About Sessions
Session in Java: https://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpSession.html  
if you use only requests, then you need to specify the current cookies in the request