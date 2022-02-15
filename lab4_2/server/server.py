from flask import Flask, request
from flask_login import LoginManager, UserMixin, current_user, login_user, login_required, logout_user
from bet import Bet
import random

app = Flask(__name__)
app.secret_key = "secret_key"
login_manager = LoginManager()
login_manager.init_app(app)


@login_manager.user_loader
def user_loader(login):
    if login in logged_in_users:
        user = UserMixin()
        user.id = login
        return user


@app.route("/login", methods=["POST"])
def login():
    if "login" not in request.form or \
            "password" not in request.form or \
            "is_croupier" not in request.form:
        return "Login failed", 401

    login = request.form["login"]
    password = request.form["password"]
    is_croupier = request.form["is_croupier"].upper() == "TRUE"
    if login in users:
        if password == users[login]["password"]:
            if login in logged_in_users:
                return "You are already logged in", 441
            global user_croupier
            if user_croupier and is_croupier:
                return "The croupier already exists", 435
            user = UserMixin()
            user.id = login
            login_user(user)
            logged_in_users.append(login)
            if is_croupier:
                user_croupier = login
            return "You are logged in", 200
    return "Login failed", 401


@app.route("/logout")
@login_required
def logout():
    global user_croupier
    if current_user.get_id() == user_croupier:
        user_croupier = None
    logged_in_users.remove(current_user.get_id())
    logout_user()
    return "Logged out", 200


@app.route("/status")
@login_required
def get_status():
    return {
               "username": users[current_user.id]["username"],
               "money": users[current_user.id]["money"],
               "is_croupier": current_user.get_id() == user_croupier
           }, 200


@app.route("/results")
@login_required
def get_results():
    if len(results) == 0:
        return "No results", 204
    else:
        return results, 200


@app.route("/bets")
@login_required
def get_bets():
    if len(bets) == 0:
        return "No bets", 204

    _usernames = []
    _types = []
    _amounts = []
    for bet in bets:
        _usernames.append(users[bet.user_id]["username"])
        _types.append(bet._type)
        _amounts.append(bet.amount)
    return {
               "usernames": _usernames,
               "types": _types,
               "amounts": _amounts
           }, 200


@app.route("/new", methods=["POST"])
@login_required
def new_bet():
    if current_user.get_id() == user_croupier:
        return "You are croupier", 403

    f = request.form
    if not ("amount" in f and "type" in f):
        return "Invalid type or amount", 444

    try:
        amount = int(f["amount"])
        _type = int(f["type"])
    except:
        return "Invalid type or amount", 444
    if not (amount > 0 or _type in range(-2, 37)):
        return "Invalid type or amount", 444

    if users[current_user.get_id()]["money"] < amount:
        return "There is not enough money in the account", 445

    bets.append(Bet(current_user.get_id(), amount, _type))
    users[current_user.get_id()]["money"] -= amount
    return "The bet accepted", 200


@app.route("/spin")
@login_required
def spin():
    global bets, results
    if current_user.get_id() != user_croupier:
        return "You are not a croupier", 403

    number = random.randint(0, 36)
    _usernames = []
    _types = []
    _amounts = []
    _results = []
    for bet in bets:
        _usernames.append(users[bet.user_id]["username"])
        _types.append(bet._type)
        _amounts.append(bet.amount)
        prize = bet.get_prize(number)
        if prize == 0:
            _results.append("Проигрыш")
        else:
            _results.append("Выигрыш")
            users[bet.user_id]["money"] += prize

    bets = []
    results = {
        "number": number,
        "usernames": _usernames,
        "types": _types,
        "amounts": _amounts,
        "results": _results
    }
    return str(number), 200


if __name__ == "__main__":
    user_croupier = None
    results = {}
    bets = []
    users = {  # mock database (primary key is login)
        "1": {"username": "NastyaA", "password": "1", "money": 100},
        "2": {"username": "NastyaR", "password": "2", "money": 200},
        "3": {"username": "Liza", "password": "3", "money": 300}
    }
    logged_in_users = []

    app.run(host="0.0.0.0", port=5000)
