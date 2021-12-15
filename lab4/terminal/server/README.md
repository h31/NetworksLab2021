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
  "Role": "admin\user"
}
Response: {
  "Status": "OK",
  "Message": {
    "JWT": "jwt-token",
    "Location": "your current location"
  },
  "Code": {
    "Value": "200",
    "Status": "OK"
  }
} 
```

Обратите внимание, в этой и во все следующих моделях field naming policy = UpperCamelCase

Регистрация

```json
Request:
POST api/v1/auth/signup
body = {
  "Login": "login"
  "Password": "password"
  "Role": "admin\user"
}
Response: {
  "Status": "OK",
  "Message": "Registration done",
  "Code": {
    "Value": "200",
    "Status": "OK"
  }
} 
```

Ошибки

```json
{
  "Status": "Error",
  "Message": "Bad credentials",
  "Code": {
    "Value": "400",
    "Status": "Bad Request"
  }
} 
```

```json
{
  "Status": "Error",
  "Message": "There is no role like that, pls change it to user or admin",
  "Code": {
    "Value": "400",
    "Status": "Bad Request"
  }
} 
```

## Терминал

Авторизация добавляет пользователя в список ныне активных пользователей (он будет отображаться при who и сможет ходить
по папкам)
и выдает JWT-токен авторизации. Этот токен необходимо передавать с каждым запросом в этой секции в заголовке
Authorization как Bearer токен.

ls - посмотреть содержимое папки

```json
Request:
GET api/v1/terminal/ls/{location?}
Response: {
  "Status": "OK",
  "Message": {
    "response": [
      "examples", "of", "directory", "content"
    ]
  },
  "Code": {
    "Value": "200",
    "Status": "OK"
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
  "Location": "/location/you/need/to/cd/to"
}
Response: {
  "Status": "OK",
  "Message": "/you/new/location/"
  "Code" : {
    "Value": "200",
    "Status": "OK"
  }
}

Error: {
  "Status": "Bad Request",
  "Message": "Wrong location to cd",
  "Code": {
    "Value": "400",
    "Status": "Bad Request"
  }
}
```

who - посмотреть залогининых пользователей и их локейшоны

```json
Request:
GET api/v1/terminal/who
Response: {
  "Status": "OK",
  "Message": {
    "response": [
      {
        "First":"user1",
        "Second":"location of user1"
      },
      {
        "First":"user2",
        "Second":"location of user2"
      },
      {
        "First":"user3",
        "Second":"location of user3"
      },
    ]
  },
  "Code": {
    "Value": "200",
    "Status": "OK"
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
  "Code" : {
    "Value": "200",
    "Status": "OK"
  }
}

Error: {
  "Status": "Error",
  "Message": "You have not enough rights",
  "Code": {
    "Value": "403",
    "Status": "Forbidden"
  }
}
```

logout - завершить сессию

```json
Request:
GET api/v1/terminal/logout
body = {
  "UserToKill": "userName"
}
Response: {
  "Status": "OK",
  "Message": "You was killed (logout successful)"
  "Code" : {
    "Value": "200",
    "Status": "OK"
  }
}
```

Ошибки общие:

Не был передан токен:
```json
{
  "Status": "Error",
  "Message": "No token, please signIn",
  "Code": {
    "Value": "403",
    "Status": "Forbidden"
  }
} 
```

Сессия юзера была завершена админом
```json
{
  "Status": "Deleted",
  "Message": "No client with login ${username} in clients list, relogin please",
  "Code": {
    "Value": "400",
    "Status": "Bad Request"
  }
} 
```