import threading

from client import Client


def start_client(ci):
    receive_thread = threading.Thread(target=ci.receive)
    receive_thread.start()

    write_thread = threading.Thread(target=ci.write)
    write_thread.daemon = True
    write_thread.start()


if __name__ == "__main__":
    client_inst = Client()
    start_client(client_inst)
