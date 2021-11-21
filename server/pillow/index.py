from .validators import check_shape
import util.misc as _
from .pillow_error import PillowError
from enum import Enum


class Actions(Enum):
    log_in = 'log-in'
    log_out = 'log-out'
    send_message = 'send-message'
    close_server = 'close-server'


class ResponseStatus(Enum):
    OK = 100
    OK_EMPTY = 101

    ERR_REQ_DATA = 200
    ERR_REQ_FORMAT = 201
    ERR_SERVER = 202


SUCCESS_THRESHOLD = 200
request_shape = {
    'name': 'payload',
    'type': dict,
    'required': True,
    'top_level': True,
    'fields': [
        {
            'name': 'action',
            'type': str,
            'required': True,
            # logOut and closeServer can only be sent by the server
            'choices': [Actions.log_in.value, Actions.send_message.value]
        },
        {
            'name': 'data',
            'type': dict,
            'required': False
        }
    ]
}
send_message_shape = {
    'name': 'data',
    'type': dict,
    'required': True,
    'fields': [
        {
            'name': 'message',
            'type': str,
            'required': False
        },
        {
            'name': 'attachment',
            'type': dict,
            'required': False,
            'fields': [
                {
                    'name': 'file',
                    'type': bytes,
                    'required': True
                },
                {
                    'name': 'name',
                    'type': str,
                    'required': True
                }
            ]
        }
    ]
}
log_in_shape = {
    'name': 'data',
    'type': dict,
    'required': True,
    'fields': [
        {
            'name': 'username',
            'type': str,
            'required': True
        }
    ]
}
chunks_shape = {
    'name': 'data',
    'type': dict,
    'required': True,
    'fields': [
        {
            'name': 'chunks',
            'type': int,
            'required': True
        }
    ]
}


def is_error(status_code: int) -> bool:
    return status_code >= SUCCESS_THRESHOLD


def validate_send_message(data):
    errors = check_shape(data, send_message_shape)
    if data is not None and not data.get('message', None) and not data.get('attachment', None):
        errors['data'] = ['Either a message or an attachment must be present']
    if not _.is_empty(errors):
        raise PillowError(errors)


def validate_log_in(data):
    errors = check_shape(data, log_in_shape)
    if not _.is_empty(errors):
        raise PillowError(errors)


def validate_chunks(data):
    errors = check_shape(data, chunks_shape)
    if not _.is_empty(errors):
        raise PillowError(errors)

    if data['chunks'] < 0:
        raise PillowError({'data': ['chunks: must be a positive Integer']})


def validate_request(payload):
    payload_errors = check_shape(payload, request_shape)
    if not _.is_empty(payload_errors):
        raise PillowError(payload_errors)

    action = payload['action']
    data = payload['data']
    globals()[f'validate_{_.snake_case(action)}'](data)

    return payload
