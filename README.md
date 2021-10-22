# Lab1: Console Chat
## Описание:
**Небольшой** консольный чат на сокетах.

Проект реализован на языке Python с использованием библиотеки **socket** и тредов.
В основе лежит идея передачи сообщений и файлов побитово, для этих целей был разработан
протокол обмена основанный на заголовках.

## Основные команды:
- !exit (со стороны клиента) - разрывает существующее соединение.
- !upload filename.jpg - отправляет файл под названием filename.jpg всем подключенным пользователям;
На данный момент могут быть отправлены **файлы любого расширения**, размер ограничивается параметром HEADER. _**Будьте осторожны при открытии присланных файлов!**_

## Как запустить проект:
Перед разворачиванием проекта настройте Config Vars на вашем сервере. 
Они должны содержать такие переменные:
- HEADER
- IP
- PORT
- ENCODING

Клонировать репозиторий и перейти в него в командной строке:
```
git clone https://github.com/KazuruK/NetworksLab2021.git
```
```
cd NetworksLab2021
```
Cоздать и активировать виртуальное окружение:
```
python3 -m venv env
```
```
source env/bin/activate
```
Установить зависимости из файла requirements.txt:
```
python3 -m pip install --upgrade pip
```
```
pip install -r requirements.txt
```
Запустить сервер:
```
python3 server.py
```
Запустить клиент:
```
python3 client.py
```

## Описание протокола
![alt text](https://lh3.googleusercontent.com/fife/AAWUweXitYiGyny_U7HqzlR8A8NIZ6tgxm1tJSBO4Rnw__EM5Y0rmNJRSn7rPrS-iE73D3DlX9FhvL7ZocP_ShGIo4WidPXHhYWMsiErkJDY46byThE351SQ0KziWnTnSU904LWgpCZkvulh0jUV4oa3AFxlXzdHZWDKmEpmdk0WsvpDCNauEJfxvoW_XB9Q5VvIPHEoAsOViKvwtxkciOzyt2mvAbvovVUAvmkGpnjlCWFReNEmh3XA4ldvylqG3bSlPQohLoVNjvM_j1psFabk-AhUv7a_Xgd2Ltov9oylpS7GBVvuPDi0JtzDHXv89pkc0rmK6g9u1erDrkfX2w1nv9Gc9K_P2SZ22ujHSUAdP8wGEOyAWQVjjeA0aDC63B1_HZKTT6C8QNw9gZCbwa0XPgnX6vRKg3bsHFNM9QrJFINFIeH9iWGPw9uKuNGMCc4fLtRuqbMxzGDQdLjZHYHvd8oP0rek-K6BdOJUG-NSDq2y2Lb1Ymj9TMvhSer23cFv2KS_kmUaf2xQ94t_9ttl8HbfGXw-e3tOlUoHa-ebiFiyMymT5J-zGx58Xc25pjJ_frENhDqLf_B7eF4_WINo3IBOegBvEUoNa8Flvnii0SUqiQClB6Vkdwdk01T9fx1jDqn2Z7xXIYmyJ6lFzjZKoifoMCdJcdmzPmNkEdiviNMW-PbB1-xIxSVq8yKhuoJ6kfjLwGGYU2RszF1rWjJceVjlbpnff0FUzQ=w1920-h937-ft "Примеры сообщений")

**HEADER** - число определяющее длину записи колличества символов, в котором может быть
закодировано сообщение.

К примеру при HEADER = 3, максимальным колличеством символов является '999' - 
текст из 1000 символов не пройдет. Данное ограничение является **общим для всех типов данных**
возможных к передаче (user, message, time, file), поэтому следует выбирать число
по необходимому **максимальному размеру загружаемого файла**. Все данные в HEADER
хранятся в байт-коде, как и передаваемые сообщения.

## Методы библиотеки socket

- ```socket()``` - создание нового сокета с указанием семейства адресов и типа
- ```setsockopt()``` - определение уровня, имени опции и размера буфера в байтах
- ```bind()``` - привязывание сокета к адресу
- ```listen()``` - запуск режима прослушивания с ограничением максимальной очереди подключений
- ```connect()``` - подключение к удаленному сокету по адресу
- ```accept()``` - принятие нового подключения, возвращает сокет и адрес клиента
- ```send()``` - отправка данных на удаленный сокет
- ```recv()``` - чтение определенного количества байт данных с сокета
- ```shutdown(socket.SHUT_RDWR)``` - отключение сокета на чтение и отправку данных
- ```close()``` - сокет помечается закрытым, все дальнейшие действия с ним невозможны

## Технические требования
Все необходимые пакеты перечислены в **requirements.txt**


###### Над проектом работал:
- **Есин Никита** - https://vk.com/kazuru