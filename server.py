import time, math
from flask import Flask, request
from flask_login import LoginManager, UserMixin, current_user, login_user, login_required, logout_user

app = Flask(__name__)
app.secret_key = "secret_key"
login_manager = LoginManager()
login_manager.init_app(app)

users ={"Bogdan":"123", "Bogunto":"111", "login":"password"}
authorized_users = []

@login_manager.user_loader
def user_loader(login):
    if login in authorized_users:
        user = UserMixin()
        user.id = login
        return user

@app.route('/login', methods=['POST'])
def login():
    data = request.json
    login = data["login"]
    if login in users:
        if data["password"] == users[login]:
            authorized_users.append(login)
        user = UserMixin()
        user.id = login
        login_user(user)
    if current_user.is_authenticated:
        return "Authorization was successful", 200
    else:
        return "Authorization failed", 401    

@app.route('/logout', methods=['GET'])
@login_required
def logout():
    if current_user.is_authenticated:
        authorized_users.remove(current_user.get_id())
        logout_user()
        return "Logout was successful", 200

@app.route('/fast/<operation>', methods=['POST'])
@login_required
def fast(operation):
    data = request.json 
    if operation == "sum":
        data["result"] = data["operand1"] + data["operand2"]
        return data, 200
    if operation == "sub":
        data["result"] = data["operand1"] - data["operand2"]
        return data, 200
    if operation == "mul":
        data["result"] = data["operand1"] * data["operand2"]
        return data, 200
    if operation == "div":
        data["result"] = data["operand1"] / data["operand2"]
        return data, 200

@app.route('/slow/<operation>', methods=['POST'])
@login_required
def slow(operation):
    data =request.json
    if operation == "fact":
        data["result"] = math.factorial(data["operand1"])
        time.sleep(4)
        return data, 200
    if operation == "sqrt":
        data["result"] = math.sqrt(data["operand1"])
        time.sleep(4)
        return data, 200

if __name__ == "__main__":
    app.run(host = 'localhost', port= 5000,debug = True)