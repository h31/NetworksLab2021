import socket
import threading
from collections import deque
from msg_types import MsgTypes
from server_messages import ServerMessage

FORMAT = "utf-8"
PORT = 5005
SERVER = socket.gethostbyname(socket.gethostname())
ADDR = (SERVER, PORT)
print(SERVER)

clients_list = {}
clients_online = []

msg_to_reassembly_buffer = deque()
msg_to_send_buffer = deque()

server = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
server.bind(ADDR)
mutex = threading.Lock()
print(server)


def decode_message(msg):
    msg_id = int(msg[0:6])
    msg_type = int(msg[6:12])
    eom = int(msg[12:18])
    msg_length = int(msg[18:24])
    msg_body = msg[24:]
    return msg_id, msg_type, eom, msg_length, msg_body


def get_login_by_adr(addr):
    for login in clients_list:
        if clients_list[login] == addr:
            return login
    return None


def construct_message(msg_id, msg_type, eom, msg_body):
    eom = str(eom)
    msg_type = str(msg_type)
    part = str(msg_id) + " " * (6 - len(eom))
    part += str(msg_type) + " " * (6 - len(msg_type))
    part += str(eom) + " " * (6 - len(msg_type))
    body_length = str(len(msg_body))
    part += body_length + " " * (6 - len(body_length))
    part = part.encode(FORMAT)
    msg = part + msg_body if type(msg_body) == bytes else part + msg_body.encode(FORMAT)
    return msg


def getting():
    while True:
        data, addr = server.recvfrom(1024)
        print(data)

        msg_to_reassembly_buffer.append((data, addr))
        print(msg_to_reassembly_buffer)


def reassembling():
    while True:
        if len(msg_to_reassembly_buffer) > 0:
            print(msg_to_reassembly_buffer)
            data, addr = msg_to_reassembly_buffer.popleft()
            login = get_login_by_adr(addr)
            msg_id, msg_type, eom, msg_length, msg_body = decode_message(data)
            for_const = [msg_id, msg_type, eom, msg_body]
            print(decode_message(data))

            if msg_type == MsgTypes.DISCONNECT_TYPE.value and login is not None and login in clients_online:
                clients_online.remove(login)
                for_const[3] = ServerMessage.DISCONNECT_MESSAGE.value
                ans = construct_message(*for_const)
                msg_to_send_buffer.append((msg_type, login, ans, addr))

            elif msg_type == MsgTypes.LOGIN_TYPE.value:
                msg = msg_body.decode(FORMAT)
                name = msg.split()[1]
                if name not in clients_list:
                    clients_list[name] = addr
                if name not in clients_online:
                    clients_online.append(name)
                login = get_login_by_adr(addr)
                for_const[3] = ServerMessage.SUCCESSFUL_LOGIN_MESSAGE.value
                ans = construct_message(*for_const)
                msg_to_send_buffer.append((msg_type, login, ans, addr))
                print(clients_online)
                print(clients_list)

            elif login in clients_online:
                msg_to_send_buffer.append((msg_type, login, data, addr))
        else:
            continue


def sending():
    while True:
        if len(msg_to_send_buffer) > 0:
            print(msg_to_send_buffer)
            msg_type, auth, ans, addr = msg_to_send_buffer.popleft()
            log_msg = construct_message(0, MsgTypes.NAME_TYPE.value, 1, auth)
            if msg_type != MsgTypes.USUAL_TYPE.value:
                server.sendto(ans, addr)
            else:
                for login in clients_online:
                    server.sendto(log_msg, clients_list[login])
                    server.sendto(ans, clients_list[login])
        else:
            continue


def main():
    print("main")
    getter_thread = threading.Thread(target=getting)
    getter_thread.start()
    print("getter started")
    reass_thread = threading.Thread(target=reassembling)
    reass_thread.start()
    print("reass started")
    sender_thread = threading.Thread(target=sending)
    sender_thread.start()
    print("sender started")


if __name__ == "__main__":
    main()