# Сервер терминала

## Модули

- Авторизация
- Терминальные функции

## Авторизация

Авторизация

```json
Request:
POST api/v1/auth/signin
body = {
  "Login": "login"
  "Password": "password"
}
Response: {
  "Status": "OK",
  "Message": {
    "JWT": "jwt-token",
    "Location": "your current location"
  }
} 
```

Обратите внимание, в этой и во все следующих моделях field naming policy = UpperCamelCase

Регистрация

```json
Request:
POST api/v1/auth/signup
body = {
  "Login": "login",
  "Password": "password"
}
Response: {
  "Status": "OK",
  "Message": "Registration done"
} 
```

Ошибки

```json
{
  "Status": "Error",
  "Message": "Bad credentials",
} 
```

## Терминал

Авторизация выдает JWT-токен авторизации, а так же начальный location. 
Этот токен необходимо передавать с каждым запросом в этой секции в заголовке.
Location тоже необходимо хранить на клиенте и передавать на сервер в теле запроса.
Authorization как Bearer токен.

ls - посмотреть содержимое папки

```json
Request:
POST api/v1/terminal/ls
body = {
  "BasePath":"base path from where we watch",
  "Location" : "location we shall ls"
}
Response: {
  "Status": "OK",
  "Message": {
    "response": [
      "examples", "of", "directory", "content"
    ]
  }
}

Error: {
  "Status": "Bad Request",
  "Message": "Problems with location to ls",
  "Code": {
    "Value":"400",
    "Status": "Bad Request"
  }
}
``` 

cd - поменять местоположение

```json
Request:
POST api/v1/terminal/cd
body = {
  "BasePath":"base path from where we go",
  "Location" : "location we shall cd to"
}
Response: {
  "Status": "OK",
  "Message": "/you/new/location/"
}

Error: {
  "Status": "Bad Request",
  "Message": "Wrong location to cd",
}
```

who - посмотреть залогининых пользователей и их локейшоны

```json
Request:
GET api/v1/terminal/who
Response: {
  "Status": "OK",
  "Message": {
    "Response": ["user1", "user2", "userN"]
  }
}
```

kill - привилегированная команда, админ может завершить сеанс другого пользователя

```json
Request:
POST api/v1/terminal/kill
body = {
  "UserToKill": "userName"
}
Response: {
  "Status": "OK",
  "Message": "userName was killed"
}

Error: {
  "Status": "Error",
  "Message": "You have not enough rights",
}
```

logout - завершить сессию

```json
Request:
GET api/v1/terminal/logout
Response: {
  "Status": "OK",
  "Message": "You was killed (logout successful)"
}
```

Ошибки общие:

Не был передан токен: HttpStatusCode.Forbidden
```json
{
  "Status": "Error",
  "Message": "No token, please signIn"
} 
```

Сессия юзера была завершена админом: HttpStatusCode.Unauthorized
```json
{
  "Status": "Logout",
  "Message": "Your session was destroyed"
} 
```