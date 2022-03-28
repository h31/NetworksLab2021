# NetworksLab2021

Работу выполнили студенты: Никита Есин, Василий Максем

В начальный момент времени сервер TFTP ожидает запросов от своих клиентов.
Если одному из клиентов необходимо обратиться к файлам, находящимся на TFTP
сервере, то он отправляют запрос RRQ к TFTP серверу. Если данный файл имеется
на сервере и к нему можно получить доступ, то начинается процесс передачи
и сервер отправляет клиенту первый блок, содержащий 512 байт данных

Если
запрошенный файл не доступен, то сервер генерирует сообщение об ошибки, передает его клиенту. На этом процесс передачи прекращается. Клиент получив блок
данный с номером 1 генерирует подтверждение получение ACK данного блока, и
так же дает ему номер 1. Получив данное подтверждение сервер начинает передачу
следующего блока данных. В случае, если подтверждение о получение блока данных
не было получено до истечения таймаута, сервер осуществляет повторную передачу
потерянного блока 5 раз. 

В начальный момент времени сервер TFTP ожидает запросов от своих клиентов.
Если одному из клиентов необходимо записать файл на TFTP сервер, то он отправляют запрос WRQ к TFTP серверу. Если сервер может осуществить запись данного
файла, то он генерирует сообщение ACK с номером блока равным 0, и отправляет
его запросившему клиенту, это сообщение означает согласие сервера на запись. Если
сервер не может осуществить запись данного файла, то генерируется сообщение
об ошибке, которое отправляется клиенту. На этом процесс записи завершается.
Клиент получив сообщение ACK с номером блока равным 0, начинает передачу
первых 512 байт файла на сервер, пересылая их в сообщение DATA с номером
блока 1. Если сервер получает данный блок, то он генерирует сообщение ACK с
номером блока 1. Так происходит до тех пор пока сервер не получает блок данных
размером менее 512 байт. В таком случае он подтверждает получение последнего
блока данных и запись на этом завершается.

Форматы пакетов:

![image](https://user-images.githubusercontent.com/31699049/160467954-f42d0d25-cb75-487e-ad77-1882cafdd0d1.png)

