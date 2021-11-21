import asyncio
from typing import Callable
from enum import Enum
import logger
import slip
import pillow
import util.misc as _
from toss_error import TossError


class MessagePart(Enum):
    HEADER = 'header'
    BODY = 'body'


class TossClientHolder:
    username: str = None

    curr_message_part = MessagePart.HEADER

    header_chunk_idx = 0
    to_collect = 0
    body = b''
    body_collected = False

    def __str__(self):
        return f'Client {self.username if self.username else f"#{self.key}"}'

    def __init__(self, reader: asyncio.StreamReader, writer: asyncio.StreamWriter, server_ref, key: int):
        self.reader = reader
        self.writer = writer
        self.server_ref = server_ref
        self.key = key

    def collect_header(self, data: bytes):
        sz = len(data)
        idx = 0
        while idx < sz:
            one_byte = data[idx]
            idx += 1
            meaningful_part = one_byte % 128
            self.to_collect += meaningful_part * (2 ** self.header_chunk_idx)
            self.header_chunk_idx += 1
            if one_byte < 128:  # highest bit is not 1
                self.curr_message_part = MessagePart.BODY
                self.body = b''
                logger.log(
                    comment=f'Collected the full Header for {self}, expecting {self.to_collect} bytes of Body',
                    status=logger.Status.success
                )
                break
        if idx != sz - 1:
            self.collect_body(data[idx:])

    def collect_body(self, data):
        sz = len(data)
        # for some strange cases when we receive more data then expected
        if sz > self.to_collect:
            logger.log(
                status=logger.Status.warn,
                comment=f'Received {sz - self.to_collect} bytes more then expected while collecting message body'
            )
        to_append = min(self.to_collect, sz)
        self.body += data[:to_append]
        self.to_collect -= to_append
        if self.to_collect == 0:
            logger.log(
                status=logger.Status.success,
                comment=f'Collected the full Body for {self}'
            )
            self.curr_message_part = MessagePart.HEADER
            self.header_chunk_idx = 0
            self.body_collected = True

    async def write_safely(self, to_send: dict, files: dict = None, cb: Callable = lambda: None):
        data = slip.serialize(to_send, {'data': files})
        header = slip.make_header(data)
        self.writer.write(header + data)
        await self.writer.drain()
        cb()

    async def respond(
            self, action: str,
            status: int = pillow.ResponseStatus.OK.value,
            data: dict = None,
            files: dict = None,
            cb: Callable = lambda: None
    ):
        to_send = {'status': status, 'action': action}
        if data is not None:
            to_send['data'] = data
            to_send['data']['time'] = _.now()
        await self.write_safely(to_send, files)
        cb()

    async def respond_w_err(self, action, errors, status: int, cb: Callable = lambda: None):
        to_send = {'status': status, 'data': {'errors': errors}}
        if action is not None:
            to_send['action'] = action
        await self.write_safely(to_send)
        cb()

    async def run(self):
        while True:
            chunk = await self.reader.read(1024)

            # EOF, means the client side of the socket has been closed
            if len(chunk) == 0:
                await self.server_ref.unregister_client(self)
                return

            logger.log(
                status=logger.Status.prefix,
                comment=f'Received {len(chunk)} bytes of data from {self} (collecting {self.curr_message_part.value})'
            )
            self.body_collected = False
            if self.curr_message_part == MessagePart.HEADER:
                self.collect_header(chunk)
            else:
                self.collect_body(chunk)

            if not self.body_collected:
                continue  # go on reading chunks until the whole message body / header is collected

            try:
                deserialized_body = slip.deserialize(self.body)
            except BaseException as err:
                if not isinstance(err, slip.SlipError):
                    raise err
                logger.log(
                    occasion_type=logger.OccasionType.error.value,
                    occasion_name=slip.SlipError.__name__,
                    status=logger.Status.error,
                    comment=str(err)
                )

                to_send = {
                    'status': pillow.ResponseStatus.ERR_REQ_FORMAT.value,
                    'data': {'errors': {'_err': [str(err)]}}
                }
                await self.write_safely(to_send)
                continue

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
                    comment=f'Payload validation failed with {_.w_amount(len(err.errors), "error")} for {self}'
                )
                await self.respond_w_err(action_to_respond, err.errors, pillow.ResponseStatus.ERR_REQ_DATA.value)
                continue

            action = payload['action']
            data = payload['data']

            try:
                await self.__getattribute__(f'handle_{_.snake_case(action)}')(action=action, data=data)
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
        duplicate_username = _.find(self.server_ref.clients, lambda client, *_: client.username == username)
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
