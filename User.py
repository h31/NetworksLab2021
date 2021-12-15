import flask_login


class User(flask_login.UserMixin):
    def __init__(self, coins, is_croupier):
        self.coins = coins
        self.is_croupier = is_croupier
