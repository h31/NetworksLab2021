class TossError(ValueError):
    def __init__(self, errors: dict, comment: str = None):
        self.errors = errors
        self.comment = comment if comment else '; '.join(list(errors.values()))
        super(TossError, self).__init__()
