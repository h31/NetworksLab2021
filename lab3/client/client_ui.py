import asyncio

from client import Client

if __name__ == "__main__":
    client_inst = Client()
    loop = asyncio.get_event_loop()
    loop.run_until_complete(loop.create_task(client_inst.connect()))
    loop.run_until_complete(
        asyncio.wait([loop.create_task(client_inst.receive()), loop.create_task(client_inst.write())]))
