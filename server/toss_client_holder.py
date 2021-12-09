import asyncio

import logger
from slip import SlipError, SlipHandler, PeekEvent
import pillow
from util.misc import *
from toss_error import TossError


class TossClientHolder:
    username: str = None

    def __str__(self):
        return f'Client {self.username if self.username else f"#{self.key}"}'

    def __init__(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter, server_ref, key: int):
        self.reader = reader
        self.writer = writer
        self.server_ref = server_ref
        self.key = key
        self.slip_handler = SlipHandler()

        self.slip_handler.on(
            PeekEvent.collected_header,
            lambda sz: logger.log(
                comment=f'Collected the full Header for {self}, expecting {w_amount(sz, "byte")} of Body',
                status=logger.Status.success
            )
        )
        self.slip_handler.on(
            PeekEvent.collected_body,
            lambda: logger.log(
                comment=f'Collected the full Body for {self}',
                status=logger.Status.success
            )
        )
        self.slip_handler.on(
            PeekEvent.received_chunk,
            lambda sz, part: logger.log(
                comment=f'Received {w_amount(sz, "byte")} of data from {self} (collecting {part.value})',
                status=logger.Status.info
            )
        )
        self.slip_handler.on(
            PeekEvent.body_size,
            lambda sz: logger.log(
                comment=f'Sending {w_amount(sz, "byte")} of Body to {self}',
                status=logger.Status.info
            )
        )
        self.slip_handler.on(
            PeekEvent.header_size,
            lambda sz: logger.log(
                comment=f'Sending {w_amount(sz, "byte")} of Header to {self}',
                status=logger.Status.info
            )
        )

    async def finish(self):
        if not self.writer.is_closing():
            self.writer.close()
        await self.writer.wait_closed()

    async def write_safely(self, to_send: dict, files: dict = None, on_success: Callable = lambda: None):
        message = self.slip_handler.make_message(to_send, {'data': files})
        self.writer.write(message)
        await self.writer.drain()
        on_success()

    async def respond(
            self, action: str,
            status: int = pillow.ResponseStatus.OK.value,
            data: dict = None,
            files: dict = None,
            on_success: Callable = lambda: None
    ):
        to_send = {'status': status, 'action': action}
        if data is not None:
            to_send['data'] = data
            to_send['data']['time'] = now()
        await self.write_safely(to_send, files)
        on_success()

    async def respond_w_err(self, action, errors, status: int, on_success: Callable = lambda: None):
        to_send = {'status': status, 'data': {'errors': errors}}
        if action is not None:
            to_send['action'] = action
        await self.write_safely(to_send)
        on_success()

    async def run(self):
        while True:
            chunk = await self.reader.read(2 ** 30)

            # EOF, means the client side of the socket has been closed
            if len(chunk) == 0:
                await self.server_ref.unregister_client(self)
                return

            try:
                deserialized_body = self.slip_handler.feed(chunk)
            except BaseException as err:
                if not isinstance(err, SlipError):
                    raise err
                logger.log(
                    occasion_type=logger.OccasionType.error.value,
                    occasion_name=SlipError.__name__,
                    status=logger.Status.error,
                    comment=str(err)
                )

                to_send = {
                    'status': pillow.ResponseStatus.ERR_REQ_FORMAT.value,
                    'data': {'errors': {'_err': [str(err)]}}
                }
                await self.write_safely(to_send)
                continue

            if not deserialized_body:
                continue  # go on reading chunks until the whole message body / header is collected

            try:
                payload = pillow.validate_request(deserialized_body)
            except BaseException as err:
                if not isinstance(err, pillow.PillowError):
                    raise err

                action_to_respond = deserialized_body.get('action', None)
                logger.log(
                    occasion_type=logger.OccasionType.error.value,
                    occasion_name=pillow.PillowError.__name__,
                    status=logger.Status.error,
                    comment=f'Payload validation failed with {w_amount(len(err.errors), "error")} '
                            f'for {self}:\n{err.to_representation()}'
                )
                await self.respond_w_err(action_to_respond, err.errors, pillow.ResponseStatus.ERR_REQ_DATA.value)
                continue

            action = payload['action']
            data = payload['data']

            try:
                await self.__getattribute__(f'handle_{snake_case(action)}')(action=action, data=data)
                logger.log(
                    occasion_type=logger.OccasionType.action.value,
                    occasion_name=action,
                    comment=f'{self}: handled',
                    status=logger.Status.success
                )
            except BaseException as err:
                if not isinstance(err, TossError):
                    raise err
                logger.log(
                    occasion_type=logger.OccasionType.action.value,
                    occasion_name=action,
                    status=logger.Status.error,
                    comment=f'{self}: {err.comment}'
                )
                await self.respond_w_err(action, err.errors, pillow.ResponseStatus.ERR_REQ_DATA.value)

    async def handle_log_in(self, action, data):
        username = data.get('username', None)
        no_username = not username
        duplicate_username = find(self.server_ref.clients, lambda client, *_: client.username == username)
        if duplicate_username or no_username:
            comment_ending = 'no username' if no_username else f'duplicate username: {username}'
            err_text = 'Username can\'t be empty' if no_username else f'User with username {username} already exists'
            raise TossError({'username': [err_text]}, f'Attempt to connect with {comment_ending}')

        self.username = username
        await self.server_ref.broadcast(
            action=action,
            get_data=lambda client: {} if client.username == self.username else {'username': self.username}
        )

    async def handle_send_message(self, action, data):
        message = data.get('message', None)
        attachment = data.get('attachment', None)
        to_send = {'username': self.username}
        files = {}
        if message:
            to_send['message'] = message
        if attachment:
            to_send['attachment'] = attachment['file']
            files['attachment'] = attachment['name']

        await self.server_ref.broadcast(
            action=action, files=files, get_data=lambda *_: to_send
        )
