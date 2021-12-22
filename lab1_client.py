import socket
import threading
import time

FORMAT = "utf-8"
HEADER = 10
LOGIN_COMMAND = "!LOGIN"                        # Commands can be changed by the user, the messages are not
DISCONNECT_COMMAND = "!DISCONNECT"
ADD_FILE_COMMAND = "!ATTACH"

DISCONNECT_MESSAGE = "!DISCONNECT"
LOGIN_MESSAGE = "!LOGIN"
LOGIN_ERROR_MESSAGE = "YOU ARE NOT LOGGED INTO THE SERVER. RERUN THE EXECUTABLE FILE"
CONNECTION_ENDED_MESSAGE = "YOU ARE DISCONNECTED FROM THE SERVER"

SERVER = "185.183.98.98"
PORT = 5050
ADDR = (SERVER, PORT)

client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client.connect(ADDR)
connected = True

TIMEZONE = time.timezone

"""
Calculates local current time from the utc time we've got from server 
"""


def get_current_time(utc_time):
    offset = time.timezone
    temp = time.strptime(utc_time)
    temp = time.mktime(temp)
    temp -= offset
    temp = time.ctime(temp)
    return temp


"""
Gets messages from the server. If message started with GET_FILE_COMMAND it is getting file's size and waiting for the
file to write 
"""


def get_message(conn):
    try:
        msg_length = conn.recv(HEADER).decode(FORMAT)  # Waits til some message will come throw the socket
        msg_length = int(msg_length)
        msg = conn.recv(msg_length)
        try:
            message = msg.decode(FORMAT)
        except UnicodeDecodeError:
            message = msg
        return message
    except ConnectionAbortedError:
        return "Rerun the executable file"
    except ValueError:
        return "You haven't logged to the server"
    except ConnectionResetError:
        return "Server is not available"


"""
Analog of previous function for files
"""


def get_file(conn):
    file_length = conn.recv(HEADER).decode(FORMAT)
    print(f"file length is {file_length}")
    if not file_length:
        return -1
    else:
        file_length = int(file_length)
        print(file_length)
        file = b""
        current_length = 0
        while current_length < file_length:
            file += conn.recv(file_length - current_length)
            current_length = len(file)
            print(f"Current file length is {current_length}")
    return file


"""
Uses the previous two functions and works according to the received message's type 
"""


def message_assembly():
    while True:
        msg_type = get_message(client)
        if msg_type == "usual":
            t = get_message(client)
            current_time = get_current_time(t)
            nickname = get_message(client)
            msg = get_message(client)
            print(f"<{current_time}> [{nickname}] {msg}")
        elif msg_type == "file":
            name = get_message(client)
            image = get_file(client)
            with open(name, 'wb') as file:
                file.write(image)
        elif msg_type == "service":
            msg = get_message(client)
            if msg == LOGIN_ERROR_MESSAGE:
                print("Rerun the executable file and try to login correctly")
                break
            elif msg == CONNECTION_ENDED_MESSAGE:
                print(msg)
                break
            else:
                print(msg)


"""
Converts the image to the string of bytes
"""


def attach_file(path):
    with open(path, 'rb') as image:
        file = image.read()
        return file


"""
Atomic send function, firstly sends the length of the message, secondly the message itself
"""


def send(msg):
    try:
        if type(msg) == str:
            message = msg.encode(FORMAT)
        else:
            message = msg
        msg_length = len(message)
        send_length = str(msg_length).encode(FORMAT)
        send_length += b' ' * (HEADER - len(send_length))
        client.send(send_length)
        client.send(message)
    except ConnectionResetError:
        print("Server is not available")


"""
Sends messages to the server. If it is COMMAND sends service message and file to the server
"""


def send_message():
    while True:
        try:
            msg = input()
            if msg.startswith(LOGIN_COMMAND):
                send('service')
                send(LOGIN_MESSAGE)
                send(msg.split()[len(msg.split())-1])
            elif msg.startswith(DISCONNECT_COMMAND):
                send('service')
                send(DISCONNECT_MESSAGE)
            elif msg.startswith(ADD_FILE_COMMAND):
                path = msg.split()[1]
                image = attach_file(path)
                send('file')
                send(path)
                send(image)
            else:
                send('usual')
                send(msg)
        except ConnectionAbortedError:
            print("Rerun the executable file")


"""
Starting the threads to get and to send messages
"""

print(f"Write {LOGIN_COMMAND} [name] to login the server")

while True:
    getting_thread = threading.Thread(target=message_assembly)
    sending_thread = threading.Thread(target=send_message)
    getting_thread.start()
    sending_thread.start()
    getting_thread.join()
    sending_thread.join()