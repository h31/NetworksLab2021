import socket

from flask import Flask, request
from flask_login import LoginManager, UserMixin, current_user, login_user, logout_user, login_required

from converter import Converter

app = Flask(__name__)
app.secret_key = "secret_key"
login_manager = LoginManager()
login_manager.init_app(app)


@login_manager.user_loader
def user_loader(login):
    if login not in logged_in_users:
        return
    user = UserMixin()
    user.id = login
    return user


@app.route('/login', methods=['POST'])
def login():
    login = request.form['login']
    password = request.form['password']
    if login in users:
        if password == users[login]:
            if login not in logged_in_users:
                logged_in_users.append(login)
            user = UserMixin()
            user.id = login
            login_user(user)
    if current_user.is_authenticated:
        return "Success", 200
    else:
        return "Failed", 401


@app.route('/logout')
@login_required
def logout():
    logged_in_users.remove(current_user.get_id())
    logout_user()
    return "Logged out", 200


@app.route('/default')
@login_required
def get_default_currency():
    return converter.default_currency, 200


@app.route('/ls')
@login_required
def get_currency_list():
    return make_response(converter.get_currency_list())


@app.route('/rate')
@login_required
def get_currency_exchange_rate():
    return make_response({
        "rate": converter.get_currency_exchange_rate(request.args.get("currency")),
        "time": converter.time
    })


@app.route('/convert')
@login_required
def convert():
    return make_response({
        "amount": converter.convert(float(request.args.get("amount")),
                                    request.args.get("from"), request.args.get("to")),
        "time": converter.time
    })


def make_response(default):
    match converter.error:
        case 0:
            return default, 200
        case 1:
            return "Unknown currency", 404
        case 2:
            return "No connection to the database", 505


def get_local_IP():
    return socket.gethostbyname(socket.gethostname())


if __name__ == "__main__":
    users = {
        "NastyaA": "123",
        "NastyaR": "456",
        "Liza": "789",
        "admin": "admin"
    }
    logged_in_users = []
    converter = Converter()

    app.run(host=get_local_IP(), port=5000)
