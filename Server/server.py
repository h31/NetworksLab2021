import socket
import threading
from datetime import datetime
import os

HEADER_LENGTH = 10  # chiếu dài header

IP = "127.0.0.1"  # địa chỉ localhost
PORT = 10000  # số port để nghe , số port > 1023
clients = {}  # danh sách những khách hàng
SEND_FILE = "SEND_FILE"
SEPARATOR = "<SEPARATOR>"

CONNECT = "CONNECT"  # tạo ra const đối với sử dụng chúng sau để hiểu loại của hoạt động khách hàng
DISCONNECT = "DISCONNECT"  # xem trên


def main():  # chức năng chính
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  # tạo ra INET, STREAMing socket
    server.bind((IP, PORT))  # kết nối cho địa chỉ với port
    server.listen(5)  # lựa chọn bao nhiếu khách hàng có thể đợi truy cập với server
    print(" Server start work!!! ")
    try:  # sử dụng "try" đối với nắm bắt keyboardInterrupt , ví dụ nếu muốn tắt server
        while True:  # làm "while true" bởi vì muốn luôn luôn làm việc , ngoại trừ tình huống khi muốn tắt server
            (clientsocket, address) = server.accept()  # chấp nhận sự kết nối từ ngoài
            client = getMessage(clientsocket)  # đọc tên
            if client:  # nếu đã chấp nhận tên
                if client in clients.values():
                    # nếu có một khách hàng với tên này, thì cần phải ngắt kết nối, để tránh tình huống khi có hai
                    # khách hàng với các tên giống nhau
                    code_n = f'{DISCONNECT:<{HEADER_LENGTH}}'.encode('utf-8')
                    notice = f"Xin lỗi, có khách hàng với tên {client['data'].decode('UTF-8')} rồi," \
                             f"cần phải lựa chọn tên khác".encode('utf-8')
                    notice_header = f"{len(notice):<{HEADER_LENGTH}}".encode('utf-8')
                    message = code_n + notice_header + notice
                    clientsocket.send(message)
                    clientsocket.close()
                else:
                    clients[clientsocket] = client  # thêm khách hàng mới vào danh sách những khách hàng
                    time = datetime.now().strftime("%H:%M")  # nhận thời gian hiện tại
                    name = client['data'].decode('UTF-8')
                    print(
                        f"At {time}, {name} new client connected ")
                    #  thông báo rằng, mới có khách hàng đã kết nối
                    notificationForClient(CONNECT,
                                          clientsocket)  # thông báo cho những khách hàng khác , rằng khách hàng mới
                    # đã kết nối
                    handler_thread = threading.Thread(target=handle_client,
                                                      args=(clientsocket,))  # tạo ra thread mới cho khách hàng mới
                    handler_thread.start()  # bắt đầu thread
    except KeyboardInterrupt:  # nếu muốn tắt server
        for cl in clients:  # cho mỗi khách hàng
            cl.shutdown(socket.SHUT_WR)  # tắt khách hàng
            cl.close()
        server.shutdown(socket.SHUT_WR)  # tắt server
        server.close()
        os._exit(0)  # ngừng lập trình


def sendToAll(msg, clientsocket):  # chứn năng đối với truền thông báo cho tất cả khách hàng
    for client in clients:  # cho mỗi khách hàng
        if client != clientsocket:  # không cần gửi thông báo của khách hàng cho mình
            client.send(msg)  # sự gửi


def notificationForClient(type, clientsocket):  # chức năng đối với sự gửi cảnh báo cho tất cả khách hàng
    code_n = f'{type:<{HEADER_LENGTH}}'.encode('utf-8')  # tạo ra code cho những khách
    notice: bytes  # tạo ra biến cho tin nhắn
    name = clients[clientsocket]['data'].decode('utf-8')  # đọc tên của khách hàng
    if type == CONNECT:
        notice = f"{name} join chat ".encode('utf-8')
    if type == DISCONNECT:
        notice = f"{name} closed chat ".encode('utf-8')
    notice_header = f"{len(notice):<{HEADER_LENGTH}}".encode('utf-8')  # tạo ra header của tin nhắn
    message = code_n + notice_header + notice  # tạo ra thông báo toàn bộ
    sendToAll(message, clientsocket)  # gửi thông báo


def getMessage(clientsocket):  # chức năng đối với chấp nhận các thông báo từ những khách hàng khác
    try:
        message_header = clientsocket.recv(HEADER_LENGTH)  # đọc header của thông báo
        if not len(message_header):  # nếu không có header thì một lỗi đã xảy ra
            return False
        if message_header.decode('utf-8') == SEND_FILE:  # kiểm tra sẽ nhận file hoặc không
            file_header_len = int(clientsocket.recv(HEADER_LENGTH).decode())
            file_header = clientsocket.recv(file_header_len)  # đọc header của file
            filename, filesize = file_header.decode('utf-8').split(
                SEPARATOR)  # tách header của file để có tên của file và size
            filename = os.path.basename(filename)  # hàm này cắt bớt đường dẫn đến tệp, chỉ để lại tên tệp
            filesize = int(filesize)  # str -> int
            bytes_read = clientsocket.recv(filesize)  # đọc cả file
            return {
                'sendFile': True,
                'header': file_header,
                'data': bytes_read
            }
        else:
            messsage_length = int(message_header.decode('utf-8').strip())  # tạo ra chiếu dài
            return {
                'sendFile': False,
                'header': message_header,
                'data': clientsocket.recv(messsage_length)}  # đưa header và hạn chế bao nhiều byte cần phải đọc
    except ValueError:  # nếu có vấn đề với loại thông tin, thì một lỗi đâ xảy ra
        print("Loại của header cần phải sẽ integer // Тип header должен быть int")
        return False
    except:  # nếu các vấn đề khách đã xảy ra
        return False


def handle_client(clientsocket):
    while True:
        message = getMessage(clientsocket)  # đọc thông báo
        name = clients[clientsocket]['data'].decode('utf-8')  # đọc tên
        current_time = datetime.now().strftime("%H:%M")  # tạo ra thời gian hiện tại
        if message is False:  # nếu vấn đề đã xảy ra ( tại sao vấn đề đã xảy ra -  xem chức năng "client" )
            clientsocket.shutdown(socket.SHUT_WR)  # tắt khách hàng
            clientsocket.close()
            print(f"{name} disconnected")
            notificationForClient(DISCONNECT,
                                  clientsocket)  # gửi tin nhắn cho những khách hàng khác , rằng một khách hàng đã
            # ngắt kết nối
            del clients[clientsocket]  # hủy bỏ khác hàng này từ danh sách những khách hàng
            return None
        if message['sendFile']:  # nếu có loại SEND_FILE
            final_message = f'{SEND_FILE:<{HEADER_LENGTH}}'.encode('utf-8') + clients[clientsocket]['header'] + \
                            clients[clientsocket]['data'] + f'{len(message["header"]):<{HEADER_LENGTH}}'.encode(
                'utf-8') + message["header"] + message["data"]
            sendToAll(final_message,clientsocket)
            # f'{SEND_FILE:<{HEADER_LENGTH}}'.encode('utf-8') - Nói cho tất cả khách hàng rằng sẽ gửi file
            # clients[clientsocket]['header'] + clients[clientsocket]['data'] - # gửi tến của khách hàng, ai đã gửi file này
            # f'{len(message["header"]):<{HEADER_LENGTH}}'.encode('utf-8') - # gửi chiếu dài của header
            # message["header"] - gửi header của file ( tên của file và size )
            # message["data"] - # gửi file
            filename = message["header"].decode('utf-8').split(SEPARATOR)[0]  # đọc tên của file để tạo ra tin nhắn
            print(
                f"At {current_time} client {name} sended file {filename}")
            # viết tin nhắn rằng máy chủ đã chấp nhận tập tin
        else:
            print(
                f'At {current_time}  received file from {name}: {message["data"].decode("utf-8")}')
            # viết tin nhắn rằng máy chủ đã chấp nhận thông báo
            final_message = clients[clientsocket]['header'] + clients[clientsocket]['data'] + message['header'] + \
                            message['data']  # tạo ra thông báo cuối cùng
            sendToAll(final_message, clientsocket)  # gửi thông báo cho tất cả khách hàng


if __name__ == '__main__':  # để bắt đầu lập trình
    main()
