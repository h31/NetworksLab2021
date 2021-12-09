# coding=utf-8
import socket
from datetime import datetime
import os
import time
import asyncio

HEADER_LENGTH = 10
IP = "0.0.0.0"
PORT = 10000
clients = {}
SEND_FILE = "SEND_FILE"
SEPARATOR = "<SEPARATOR>"
CONNECT = "CONNECT"
DISCONNECT = "DISCONNECT"
UID = "c97ec0d1-df22-41f4-858f-7beee9e1bbc4".encode("utf-8")
clients = []
clientsNames = []


async def handle(reader, writer):
    try:
        clients.append(writer)
        header = await reader.read(HEADER_LENGTH)
        headerLen = int(header.decode('utf-8'))
        name = await reader.read(headerLen)
        name = name.decode('utf-8')
        headerName = f'{len(name):<{HEADER_LENGTH}}'.encode('utf-8')
        current_time = datetime.now().strftime("%H:%M")
        if name not in clientsNames:
            clientsNames.append(name)
            print(f"In {current_time} connected new client - {name}")
            notificationForClient(CONNECT, writer, name)
        else:
            code_n = f'{DISCONNECT:<{HEADER_LENGTH}}'.encode('utf-8')
            notice = f"Sorry, already have a client with this name: {name} " \
                     f"Need choose other name".encode('utf-8')
            notice_header = f"{len(notice):<{HEADER_LENGTH}}".encode('utf-8')
            message = code_n + notice_header + notice
            writer.write(message)
            writer.close()
        while True:
            try:
                header = await reader.read(HEADER_LENGTH)
                current_time = datetime.now().strftime("%H:%M")
                encodedHeader = header.decode('utf-8')
                if encodedHeader.strip(' ') != SEND_FILE:
                    headerLen = int(encodedHeader)
                    message = await reader.read(headerLen)
                    print(
                        f'in {current_time} recieved message from {name}: {message.decode("utf-8")}')
                    finalMessage = headerName + name.encode('utf-8') + header + message
                    sendToAll(finalMessage, writer)
                else:
                    fileHeaderHeaderLen = await reader.read(HEADER_LENGTH)
                    fileHeaderLen = int(fileHeaderHeaderLen.decode("utf-8"))
                    fileHeader = await reader.read(fileHeaderLen)
                    filename, filesize = fileHeader.decode('utf-8').split(
                        SEPARATOR)
                    filesize = int(filesize)
                    totalBytes = bytes()
                    try:
                        while not UID in totalBytes:
                            bytesRead = await reader.read(filesize)
                            totalBytes += bytesRead
                    finally:
                        sendFileFlag = f'{SEND_FILE:<{HEADER_LENGTH}}'.encode('utf-8')
                        fileMessage = sendFileFlag + headerName + name.encode(
                            'utf-8') + fileHeaderHeaderLen + fileHeader + totalBytes
                        print(
                            f"In {current_time} client {name} send file {filename}")
                        sendToAll(fileMessage, writer)
            except:
                notificationForClient(DISCONNECT, writer, name)
                clients.remove(writer)
                clientsNames.remove(name)
                writer.close()
    except:
        try:
            notificationForClient(DISCONNECT, writer, name)
            clients.remove(writer)
            clientsNames.remove(name)
            writer.close()
            print(f'disconect client {name} voi error')
        except:
            print(f'client {name} disconnected')


async def main():
    while True:
        server = await asyncio.start_server(
            handle, IP, PORT)
        print('Server started !!!')
        async with server:
            await server.serve_forever()


def sendToAll(msg, clientsocket):
    for client in clients:
        if client != clientsocket:
            client.write(msg)


def notificationForClient(type, clientsocket, name):
    if name in clientsNames:
        code_n = f'{type:<{HEADER_LENGTH}}'.encode('utf-8')
        notice: bytes
        if type == CONNECT:
            notice = f"{name} join chat ".encode('utf-8')
        if type == DISCONNECT:
            notice = f"{name} closed chat ".encode('utf-8')
        notice_header = f"{len(notice):<{HEADER_LENGTH}}".encode('utf-8')
        message = code_n + notice_header + notice
        sendToAll(message, clientsocket)


asyncio.run(main())
