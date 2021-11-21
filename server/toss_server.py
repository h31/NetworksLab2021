import socket
from signal import SIGINT, SIGTERM, signal
import asyncio
import logger
from toss_client_holder import TossClientHolder
from typing import List, Callable
from pillow import Actions, ResponseStatus
import util.misc as _


class GracefulExit(SystemExit):
    code = 1


class TossServer:
    clients: List[TossClientHolder] = []

    def __init__(self, address, port):
        self.address = address
        self.port = port

    def get_running_on(self) -> str:
        return ', '.join(f'{sock.getsockname()[0]}:{sock.getsockname()[1]}' for sock in self.sock.sockets)

    def __raise_graceful_exit(self, *_):
        self.sock.get_loop().stop()
        raise GracefulExit()

    def get_handling_now(self):
        return f'Handling {_.w_amount(len(self.clients), "connection")} now'

    async def broadcast(
            self, action: str,
            filter_clients: Callable[[List[TossClientHolder]], List[TossClientHolder]] = lambda lst: lst,
            get_data: Callable[[TossClientHolder], dict] = lambda *_: None,
            files: dict = None,
            get_status: Callable[[TossClientHolder], int] = lambda *_: ResponseStatus.OK.value,
            cb: Callable = lambda: None
    ):
        if files is None:
            files = {}

        writeable_clients = list(filter(lambda c: not c.writer.is_closing(), self.clients))
        filtered_clients = filter_clients(writeable_clients)
        amount = len(filtered_clients)
        error_count = 0

        logger.log(comment=f'Broadcasting to {_.w_amount(amount, "client")}...', status=logger.Status.prefix)

        time = _.now()  # So that the time is the same for each client
        for client in filtered_clients:
            to_send = {'action': action, 'status': get_status(client)}
            data = get_data(client)
            if data is not None:
                to_send['data'] = data
                to_send['data']['time'] = time
            try:
                await client.write_safely(to_send, files)
            except IOError as err:  # For the rare case when some client closes the connection while broadcasting
                logger.log(
                    status=logger.Status.error,
                    comment=f'Error sending data to {client}: {str(err)}',
                )
                error_count += 1

        logger.log(
            comment=f'Finished broadcasting with {_.w_amount(error_count, "error")}',
            status=logger.Status.warn if error_count else logger.Status.success
        )
        cb()

    async def close_completely(self, err: Exception = None):
        if err:
            logger.log(
                occasion_type=logger.OccasionType.error.value,
                occasion_name=_.get_clean_type(err),
                status=logger.Status.error,
                comment=str(err)
            )

        waiting_for = len(self.clients)
        if waiting_for:
            logger.log(
                comment=f'Closing the server, waiting for {_.w_amount(waiting_for, "client")} to disconnect...',
                status=logger.Status.prefix
            )
            await self.broadcast(
                action=Actions.close_server.value,
                get_status=lambda *_: ResponseStatus.OK_EMPTY.value
            )

            for client in self.clients:
                if not client.writer.is_closing():
                    client.writer.close()
                    await client.writer.wait_closed()

        self.sock.close()
        await self.sock.wait_closed()
        logger.log(comment=f'Toss Server has been closed', status=logger.Status.info)

    async def __register_client(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter):
        client = TossClientHolder(reader, writer, self, len(self.clients) + 1)
        self.clients.append(client)
        logger.log(
            status=logger.Status.success,
            comment=f'Established a new connection. {self.get_handling_now()}'
        )
        try:
            await client.run()
        except BaseException as err:  # All the known exceptions are handled within the Client
            logger.log(
                occasion_type=logger.OccasionType.error.value,
                occasion_name=_.get_clean_type(err),
                status=logger.Status.error,
                comment=f'{client}: {str(err)}'
            )
            if not writer.is_closing():
                to_send = {
                    'status': ResponseStatus.ERR_SERVER.value,
                    'data': {'errors': {'_err': 'Server error'}}
                }
                await client.write_safely(to_send)

    async def unregister_client(self, client):
        self.clients = _.l_filter(lambda c: c != client, self.clients)
        logger.log(
            status=logger.Status.info,
            comment=f'Closed connection for {client}. {self.get_handling_now()}'
        )
        await self.broadcast(action=Actions.log_out.value, get_data=lambda *_: {'username': client.username})

    async def __run(self):
        # AF_INET means we use address + port for address
        # SOCK_STREAM means this is a TCP socket - set by default internally
        self.sock = await asyncio.start_server(
            client_connected_cb=self.__register_client,
            host=self.address,
            port=self.port,
            family=socket.AF_INET,
            start_serving=False
        )

        signal(SIGINT, self.__raise_graceful_exit)
        signal(SIGTERM, self.__raise_graceful_exit)
        running_on = self.get_running_on()
        logger.log(comment=f'Toss Server is running on {running_on}', status=logger.Status.info)

        try:
            await self.sock.serve_forever()
        except GracefulExit:
            pass
        finally:
            await self.close_completely()

    def run(self):
        asyncio.run(self.__run())
