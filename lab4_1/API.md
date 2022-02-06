## Converter API
- POST /login
  - FORM{"login": "<login>", "password": "<password>"}
  - Responses:
    - Code: 200
      - JSON{"answer": "Success"}
    - Code: 401
      - JSON{"answer": "Failed"}
- GET /logout
  - Responses:
    - Code: 200
      - JSON{"answer": "Logged out"}
    - Code: 401
      - JSON{"answer": 'You are not logged in'}
- GET /converter
  {"cmd": "default"}
  - Response:
      - JSON{"answer": "<answer>"}
  {"cmd": "ls"}
  - Response:
      - JSON{"answer": "<answer>"}
  {"cmd": "rate", "currency": "<currency_ticker>"}
  - Responses:
    - Code: 200
      - JSON{"answer": "<answer>", "time": <time>}
    - Code: other
      - JSON{"answer": "<answer>"}
  {"cmd": "convert", "amount": "<amount>", "from": "<currency_ticker>", "to": "<currency_ticker>"}
  - Responses:
    - Code: 200
      - JSON{"answer": "<answer>", "time": <time>}
    - Code: other
      - JSON{"answer": "<answer>"}
