from .validators import check_shape
import util.misc as _
from .pillow_error import PillowError

SUCCESS_THRESHOLD = 200
response_status = {
    'OK': {'code': 100, 'comment': 'Client data received, answer sent'},
    'OK_EMPTY': {'code': 101, 'comment': 'Client data received, no data to send back'},
    'ERR_REQ_DATA': {'code': 200, 'comment': 'Invalid request data'},
    'ERR_REQ_FORMAT': {'code': 201, 'comment': 'Invalid request format'},
    'ERR_SERVER': {'code': 202, 'comment': 'Server error'}
}
actions = {
    'log_in': 'log-in',
    'log_out': 'log-out',
    'send_message': 'send-message',
    'chunks': 'chunks',
    'close_server': 'close-server'
}
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
            'choices': [actions['log_in'], actions['send_message'], actions['chunks']]
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
