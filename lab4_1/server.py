import socket
from flask import Flask, request
from flask_login import LoginManager, UserMixin, current_user, login_user, logout_user
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
        return {"answer": "Success"}, 200
    else:
        return {"answer": "Failed"}, 401


@app.route('/logout')
def logout():
    if current_user.is_authenticated:
        logged_in_users.remove(current_user.get_id())
        logout_user()
        return {"answer": "Logged out"}, 200
    else:
        return {"answer": 'You are not logged in'}, 401


@app.route('/converter')
def converter():
    if not current_user.is_authenticated:
        return {"answer": 'You are not logged in'}, 401

    args = request.args
    match args.get("cmd"):
        case "default":
            return {"answer": converter.get_default_currency()}, 200
        case "ls":
            result = converter.get_currency_list()
            return {"answer": result["list"]}, result["status_code"]
        case "rate":
            rate = converter.get_currency_exchange_rate(args.get("currency"))
            if rate["status_code"] == 200:
                return {"time": converter.get_last_update_time(), "answer": rate["rate"]}, 200
            else:
                return {"answer": rate["rate"]}, rate["status_code"]
        case "convert":
            result = converter.convert(float(args.get("amount")), args.get("from"), args.get("to"))
            if result["status_code"] == 200:
                return {"time": converter.get_last_update_time(), "answer": result["result"]}, 200
            else:
                return {"answer": result["result"]}, result["status_code"]
        case _:
            return {"answer": "Invalid command"}, 400


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
