from json import dumps


class PillowError(ValueError):
    errors = {}

    def __init__(self, errors: dict):
        self.errors = errors
        super(dumps(errors))
