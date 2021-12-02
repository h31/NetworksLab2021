import asyncio

from client import Client

if __name__ == "__main__":
    loop = asyncio.get_event_loop()
    client_inst = Client(loop)
    loop.run_until_complete(loop.create_task(client_inst.connect()))
    loop.create_task(client_inst.receive())
    loop.create_task(client_inst.write())
    loop.run_forever()
