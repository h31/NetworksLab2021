import unittest
from pillow import validate_request, PillowError, actions


class MyTestCase(unittest.TestCase):
    def assertRaisesProperly(self, func, err_obj, **func_args):
        raised = False
        try:
            func(**func_args)
        except PillowError as p_err:
            self.assertEqual(err_obj, p_err.errors)
            raised = True
        self.assertTrue(raised)

    def test_shape_validation(self):
        messed_up_fields = {
            'data': {'username': 'Jack Sparrow', 'is_captain': True},
            'status': 100
        }
        err_obj_1 = {
            'action': ['This field is required'],
            'payload': ['status: This field is not supported']
        }
        self.assertRaisesProperly(
            func=validate_request,
            err_obj=err_obj_1,
            payload=messed_up_fields
        )

        messed_up_types = {
            'action': 37.1,
            'data': 'Basically dead by now'
        }
        err_obj_2 = {
            'action': ['Expected a str, got float'],
            'data': ['Expected a dict, got str']
        }
        self.assertRaisesProperly(
            func=validate_request,
            err_obj=err_obj_2,
            payload=messed_up_types
        )

        messed_up_choices = {
            'action': 'find-the-chest',
            'data': {'what_we_have': 'A drawing of a key'}
        }
        err_obj_3 = {
            'action': ['Unsupported value find-the-chest, expected one of log-in, send-message, chunks']
        }
        self.assertRaisesProperly(
            func=validate_request,
            err_obj=err_obj_3,
            payload=messed_up_choices
        )

    def test_action_specific_validation(self):
        log_in_payload = {
            'action': actions['log_in'],
            'data': {'message': 'Are you afraid of death?'}
        }
        err_obj_1 = {
            'data': [
                'message: This field is not supported',
                'username: This field is required'
            ],
        }
        self.assertRaisesProperly(
            func=validate_request,
            err_obj=err_obj_1,
            payload=log_in_payload
        )

        send_message_payload = {
            'action': actions['send_message'],
            'data': {}
        }
        err_obj_2 = {
            'data': ['Either a message or an attachment must be present']
        }
        self.assertRaisesProperly(
            func=validate_request,
            err_obj=err_obj_2,
            payload=send_message_payload
        )

    def test_valid_payload(self):
        valid_payload = {
            'action': actions['send_message'],
            'data': {
                'message': 'Call the Kraken!',
                'attachment': {'file': bytes([1, 2, 3, 4]), 'name': 'davy-jones-theme.mp3'}
            }
        }
        self.assertEqual(valid_payload, validate_request(valid_payload))


if __name__ == '__main__':
    unittest.main()
