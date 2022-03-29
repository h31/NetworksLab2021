# 1.2.12. Система торгов
## Описание работы
В основе проекта лежат данные, которые сериализуются между сервером/клиентом и обрабатываются локально.
Данные пакются с клиента на сервер в json.
Отправляем json, сервер принимает и парсит его(также и в обратную сторону)
На завершении локально обрабатываем данные.

# 4.1 - Сервер Сторона
Для начала работы/запуска сервера, установить фреймворк bottle, pprint
```
-pip install bottle
```
Запуск сервера происходит исполнением main.py

## Формат данных
```
users:
{ userId - индекс
  group - права доступа}
```
```
items:
{ itemId - индекс
  itemPrice - цена предмета  }
```

# 4.2 - Клиент Сторона
Запуск сервера происходит исполнением client.py

## Формат данных
```
userInfo:
{ userId - индекс юзера
  userGroup - права доступа юзера
  userName - имя юзера
  userPassword - пароль юзера}
```
## Структура БД
```
Items:
(
  itemId integer
  constraint items_pk
  primary key,
  itemName text,
  itemPrice integer
)
```
```
Users:
(
  userId integer 
  constraint items_pk
  primary key,
  userGroup text,
  userName text,
  userPassword text
)
```
```
Info:
(
  exchangeVersion text
)
```
