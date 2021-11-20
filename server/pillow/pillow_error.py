from json import dumps


class PillowError(ValueError):
    errors = {}

    def __init__(self, errors: dict):
        super(PillowError, self).__init__(dumps(errors))
        self.errors = errors
