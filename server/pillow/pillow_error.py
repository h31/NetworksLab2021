from json import dumps


class PillowError(ValueError):
    def __init__(self, errors: dict):
        super(PillowError, self).__init__(dumps(errors))
        self.errors = errors

    def to_representation(self):
        return '\n'.join(map(lambda item: f'  {item[0]}: {item[1]}', self.errors.items()))
