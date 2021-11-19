class SlipError(ValueError):
    def __init__(self, data: str or dict = None):
        if not data:
            message = 'Unexpected end of input'
        elif type(data) == str:
            message = data
        else:
            idx = data['idx']
            token = data['str'][idx]
            message = f'Unexpected token ({token}) at {idx}'
        super(SlipError, self).__init__(message)
