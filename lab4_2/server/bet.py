class Bet:
    def __init__(self, user_id, amount, _type):
        self.user_id = user_id
        self.amount = amount
        self._type = _type  # -2 (четное), -1 (нечетное) или 0-36

    def get_prize(self, number):
        match self._type:
            case -2:
                is_win = (number % 2) == 0 and number != 0
            case -1:
                is_win = (number % 2) == 1 and number != 0
            case _:
                is_win = number == self._type

        if not is_win:
            return 0
        else:
            match self._type:
                case -2 | -1:
                    return 2 * self.amount
                case _:
                    return 36 * self.amount
