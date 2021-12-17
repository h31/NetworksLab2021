# Forum Server

## Авторизация и регистрация

### Авторизация
Request
```http request
POST /auth/sign-in
Content-Type: application/json

{
"login": "user name",
"psw": "password"
}
```
OK Response
```http request
HTTP/1.1 200 OK

{
  "jwt": "jwt token"
}
```
Error Response
```http request
HTTP/1.1 401 Unauthorized

Error. Incorrect login or password
```

### Регистрация
Request
```http request
POST /auth/sign-up
Content-Type: application/json

{
"login": "user name",
"psw": "password"
```
OK Response
```http request
HTTP/1.1 200 OK

Success signup
```
Error Responses

- пользователь с таким именем уже существует
```http request
HTTP/1.1 403 Forbidden

User yana already exists. Try to register again
```

- остальные ошибки
```http request
HTTP/1.1 400 Bad Request

Something went wrong. Try to register again
```

## Запросы пользователя

В работе форума существует принудиельное отключение пользователя.
Это происходит если пользователь не был активен и не посылал запросы
серверу в тесение одного часа. По прошествии часа при попытке сделать
запрос клиент получит сообщение о том, что он был отключен, и просьба
подключиться снова.

### Выдача иерархического представления форума
Request
```http request
GET /forum/request/hierarchy
```
OK Response
```http request
HTTP/1.1 200 OK

{
  "response": {
    "main theme 1": [
      "sub theme 1.1",
      "sub theme 1.2",
      ...
      "sub theme 1.n"
    ],
    "main theme 2": [
      ...
    ],
    ...
    "main theme m": [
      ...
    ]
  }
}
```
Error Responses

!!! Ошибки, помечнные <span style="color:green">зеленым</span> цветом встречаются во всех запросах
- <span style="color:green">не авторизирован (jwt не подтвержден)</span>
```http request
HTTP/1.1 401 Unauthorized

<Response body is empty>
```

- <span style="color:green">пользователь был принудительно отключен</span>
```http request
HTTP/1.1 401 Unauthorized

You have been inactive for 1 hour. Login again
```
- <span style="color:green">остальные ошибки</span>
```http request
HTTP/1.1 400 Bad Request

Something went wrong. Try to register again
```
### Выдача постов форума 
Request
```http request
GET /forum/request/message-list
```
OK Response
```http request
HTTP/1.1 200 OK

{
  "messages": [
    {
      "time": "the time the client sent a message to the forum",
      "name": "user name",
      "msg": "user message"
    },
    ...
  ]
}
```
Error Responses

- тема не найдена 
```http request
HTTP/1.1 404 Not Found

No such sub theme found
```

### Написать пост в определенной теме форума

Request
```http request
GET /forum/request/message

{
  "subTheme": "sub theme",
  "msg": "message"
}
```
OK Response
```http request
HTTP/1.1 200 OK

Success
```
Error Responses

- тема не найдена
```http request
HTTP/1.1 404 Not Found

No such sub theme found
```
### Выдача списка активных пользователей

Request
```http request
GET /forum/request/active-users
```
OK Response
```http request
HTTP/1.1 200 OK

{
  "users": [
    "user 1",
    ...
    "user n"
  ]
}
```

### Обработка отключения (logout)

Request
```http request
DELETE /forum/request/logout
```
OK Response
```http request
HTTP/1.1 200 OK

You have successfully logged out
```
Error Responses

- пользовоталь уже был отключен
```http request
HTTP/1.1 401 Unauthorized

You have been inactive for 1 hour. You have already been logged out
```

