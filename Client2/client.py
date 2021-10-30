#coding=utf-8
import threading
import socket
from datetime import datetime
import time
import signal
import os
import re

HEADER_LENGTH = 10  # header chiếu dài
SEPARATOR = "<SEPARATOR>"
SEND_FILE = "SEND_FILE"

IP = "networkslab-ivt.ftp.sh"  # địa chỉ localhost
PORT = 10000  # số port được dùng bởi máy chủ
UID = "c97ec0d1-df22-41f4-858f-7beee9e1bbc4".encode("utf-8") # string riêng để hiểu rằng tập tin đã kết thúc
CONNECT = "CONNECT"  # tạo ra const đối với sử dụng chúng sau để hiểu loại của hoạt động khách hàng
DISCONNECT = "DISCONNECT"  # xem trên


def main():  # chức năng chính
    nickname = input("Write name for chat: ")  # đọc tên
    clientsocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)  # tạo ra NET, STREAMing socket
    clientsocket.connect((IP, PORT))  # kết nối cho địa chỉ với port

    def catch_interrupt(signal, frame):
        clientsocket.shutdown(socket.SHUT_WR)
        clientsocket.close()
        os._exit(0)

    signal.signal(signal.SIGINT, catch_interrupt)  # nắm dấu hiệu , rằng muốn kết thúc sự kết nối
    nickname_code = nickname.encode('utf-8')  # đọc code của tên
    nickname_header = f"{len(nickname_code):<{HEADER_LENGTH}}".encode('utf-8')  # tạo ra header cho tên
    clientsocket.send(nickname_header + nickname_code)  # gửi thông tin cho máy chủ
    receive_thread = threading.Thread(target=getMessage, args=(clientsocket,))  # tạo ra thread
    receive_thread.start()  # bắt đầu thread
    try:
        while True:  # đọi trước khi khách hàng sẽ viết tin nhắn
            message = input()  # đọc tin nhắn
            if (re.match("^send .*\.\w*\s*$", message)): # hiểu rằng muốn gửi file
                try:
                    files = re.findall("\s+\w.*\.\w*\s*", message) # tên của tập tin
                    fileName = files[0].strip() # xóa khoảng trống ở tên của tập tin
                    filesize = os.path.getsize(fileName) # đọc size của tập tin
                    clientsocket.send(f"{SEND_FILE:<{HEADER_LENGTH}}".encode("utf-8")) # thông báo cho máy chủ rằng muốn gửi tập tin
                    fileHeader = f"{files[0]}{SEPARATOR}{filesize}".encode()  # tạo ra header cho tập tin
                    clientsocket.send(f"{len(fileHeader):<{HEADER_LENGTH}}".encode('utf-8'))  # gửi header của tập tin
                    clientsocket.send(fileHeader)  # gửi header của tập tin
                    f = open(fileName, "rb") # mở tập tin để đọc
                    bytes_read = f.read(filesize) # đọc thông tin từ tập tin
                    clientsocket.sendall(bytes_read) # gửi cho máy chủ những gì trong tập tin
                    f.close() # đóng tập tin
                    clientsocket.send(UID) # gửi UID để máy chủ có thể hiểu rằng kết thúc nhận tập tin
                except:
                    print(f'Không có thể tìm kiếm {fileName}')

            else:
                if message:  # nếu khách hàng đã viết tin nhắn
                    message_code = message.encode('utf-8')  # đọc code của tin nhắn
                    message_header = f"{len(message_code):<{HEADER_LENGTH}}".encode(
                        'utf-8')  # tạo ra header cho tin nhắn
                    clientsocket.send(
                        message_header + message_code )  # gửi thông báo cho máy chủ
    except:
        os._exit(0)  # nếu vấn đề đã xảy ra thì ngắt kết nối


def getMessage(clientsocket):
    while True:
        nickname_header = clientsocket.recv(HEADER_LENGTH)  # đọc header
        current_time = datetime.now().strftime("%H:%M")
        if len(nickname_header) == 0:
            print("Close connect")
            clientsocket.shutdown(socket.SHUT_WR)
            clientsocket.close()
            os._exit(0)
        if nickname_header.decode().strip() == SEND_FILE: # nếu sẽ nhận tập tin
            name_header = clientsocket.recv(HEADER_LENGTH) # đọc header của tên khách hàng
            name_length = int(name_header.decode()) # đọc header tên chiếu dài
            name = clientsocket.recv(name_length).decode() # đọc tên của khách hàng
            file_header_len = int(clientsocket.recv(HEADER_LENGTH).decode()) # đọc chiếu dài của header của tập tin
            file_header = clientsocket.recv(file_header_len) # đọc header của tập tin
            filename, filesize = file_header.decode('utf-8').split(SEPARATOR) # tách header của file để có tên của file và size
            filename = os.path.basename(filename) # hàm này cắt bớt đường dẫn đến tệp, chỉ để lại tên tệp
            filesize = int(filesize) # str -> int
            f = open(filename, "wb") # tạo ra tập tin (cần đọc lại thông tin để hiểu kĩ hơn)
            total_bytes = bytes()
            while not UID in total_bytes: # làm việc trước khi gặp UID , khi gặp UID đây là nghĩa , rằng tập tin đã kết thúc
                bytes_read = clientsocket.recv(filesize)  # nhận bytes của tập tin
                total_bytes+=bytes_read # thêm bytes để lưu trữ tất cả những gì đã nhận
            f.write(total_bytes[:(len(total_bytes)-len(UID))]) # viết thông tin, những gì đã đọc, và xóa bỏ UID 
            f.close() # đóng tập tin
            print(f'<{current_time}> {name} send file {filename}') #
        else:
            type = nickname_header.decode('utf-8').strip()  # đọc loại của tin nhắn
            if type == CONNECT or type == DISCONNECT:  # nếu loại là sự kết nối hoặc sự ngắt kết nối thì hiển thị thông báo đặc biệt
                warning_length = int(
                    clientsocket.recv(HEADER_LENGTH).decode('utf-8').strip())  # tạo ra chiếu dài cảnh báo
                warning = clientsocket.recv(warning_length).decode('utf-8')  # tạo ra cảnh báo
                print(f'{warning}')  # viết cảnh báo
                continue
            nickname_length = int(nickname_header.decode('utf-8').strip())  # tạo ra chiếu dài cho tên
            nickname = clientsocket.recv(nickname_length).decode('utf-8')  # đọc tên
            message_header = clientsocket.recv(HEADER_LENGTH)  # đọc message header
            message_length = int(message_header.decode('utf-8').strip())  # tạo ra chiếu dài của tin nhắn
            message = clientsocket.recv(message_length).decode('utf-8')  # tạo ra tin nhắn
            print(f'<{current_time}> [{nickname}]: {message}')  # viết thông báo


if __name__ == '__main__':  # để bắt đầu lập trình
    main()


