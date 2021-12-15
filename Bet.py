class Bet:

    def __init__(self, user_id, amount, bet_type):
        self.user_id = user_id

        if amount < 1:
            raise Exception("Invalid amount")
        self.amount = amount

        if bet_type < -2 or bet_type > 36:
            raise Exception("Invalid bet type")
        self.bet_type = bet_type  # 0-36 - specific number, -1 - odd, -2 - even

    def __str__(self):
        match self.bet_type:
            case -1:
                type_str = "Odd"
            case -2:
                type_str = "Even"
            case _:
                type_str = str(self.bet_type)

        return f'''User:{self.user_id}\tType:{type_str}\tAmount:{self.amount}'''

    def getResult(self, number):
        if number < 0 or number > 36:
            raise Exception("Invalid number")
        even = (number % 2) == 0
        match self.bet_type:
            case -1:  # odd
                return (number % 2) == 1 and number != 0
            case -2:  # even
                return (number % 2) == 0 and number != 0
            case _:
                return number == self.bet_type

    def getPrize(self, number):
        result = self.getResult(number)
        if not result:
            return 0
        match self.bet_type:
            case -1:  # odd
                return self.amount * 2
            case -2:  # even
                return self.amount * 2
            case _:
                return self.amount * 36
