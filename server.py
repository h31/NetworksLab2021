import random
import flask_login  # from requirements.txt
import flask

from flask import jsonify, make_response
from Bet import Bet
from User import User

app = flask.Flask(__name__)
app.secret_key = 'qwerty'

login_manager = flask_login.LoginManager()
login_manager.init_app(app)

addr = '0.0.0.0'

users = {
    'Tsaplin': [{'password': '12345'}, 1200],
    'Jeus': [{'password': 'qwerty'}, 1500],
    'user1': [{'password': 'user1'}, 1000],
    'user2': [{'password': 'user2'}, 2000]
}


@login_manager.user_loader
def user_loader(login):
    if login not in logged_in_users:
        return
    user = logged_in_users[login]
    return user


@login_manager.request_loader
def request_loader(request):
    user_login = request.form.get('login')
    if user_login not in logged_in_users:
        return
    return logged_in_users[loginProcess]


@app.route('/')
def redirect():
    if flask_login.current_user.is_authenticated:
        return flask.redirect(flask.url_for('userInfo'))
    return flask.redirect(flask.url_for('login'))


@app.route('/login', methods=['GET', 'POST'])
def loginProcess():
    match flask.request.method:
        case 'GET':
            if flask_login.current_user.is_authenticated:
                return flask.redirect(flask.url_for('userInfo'))
            return '''
                   <form action='login' method='POST'>
                    <input type='text' name='login' id='login' placeholder='login'/>
                    <input type='password' name='password' id='password' placeholder='password'/>
                    <input type='checkbox' name='croupier' id='croupier' placeholder='croupier'/>
                    Login as croupier
                    <input type='submit' name='submit'/>
                   </form>
                   ''', 200
        case _:
            if flask_login.current_user.is_authenticated:
                return "You already login", 441  # ERROR 441 - try login twice
            form = flask.request.form
            if 'login' not in form or 'password' not in form:
                return "You must specify the login and password", 442  # ERROR 442 - Some field is not filled
            user_login = form['login']
            user_password = form['password']
            if user_login not in users:
                return bad_login(443)
            if 'croupier' in form:
                is_croupier = form['croupier'] == 'on'
            else:
                is_croupier = False
            if user_password == users[user_login][0]['password']:
                user = User(users[user_login][1], is_croupier)
                user.id = user_login

                global croupier
                if croupier is not None and is_croupier:
                    return 'the croupier already exists', 435

                if is_croupier:
                    croupier = user

                logged_in_users[user_login] = user
                flask_login.login_user(user)
                return flask.redirect(flask.url_for('userInfo'))
    return bad_login(443)


def bad_login(code):
    return f'''
        BAD LOGIN
        <a href="http://{addr}:5000/login">
           <input type="button" value="Try again" />
        </a>
    ''', code


@app.route('/userInfo')
@flask_login.login_required
def userInfo():
    doc = f'''
        <br>
        Logged in as: {flask_login.current_user.id} \n
        </br><br>
        Your coins: {flask_login.current_user.coins}
        </br>
    ''', 200

    return doc


@app.route('/userInfo/json')
@flask_login.login_required
def userInfo_json():
    data = {
        'login': flask_login.current_user.id,
        'coins': flask_login.current_user.coins,
        'is_croupier': flask_login.current_user.is_croupier
    }
    return make_response(jsonify(data), 200)


@app.route('/logout')
@flask_login.login_required
def logout():
    global croupier
    if flask_login.current_user.is_croupier:
        croupier = None
    del logged_in_users[flask_login.current_user.id]
    flask_login.logout_user()
    return 'Logged out', 200


@app.route('/bet', methods=['GET', 'POST'])
@flask_login.login_required
def bet():
    if flask_login.current_user.is_croupier:
        return "You are croupier. You can't do bets", 403
    match flask.request.method:
        case 'GET':
            return f'''
                           <form action='bet' method='POST'>
                            Type:<input type='number' min='-2' max='36' name='type' id='type' placeholder='type'/>
                            Bet amount:<input type='number' min='1' name='amount' id='amount' placeholder='amount'/>
                            <input type='submit' name='submit'/>
                           </form>
                           ''', 200
        case _:
            form = flask.request.form
            if 'amount' not in form or 'type' not in form:
                return "you must specify the correct type and amount", 444
            amount = int(form['amount'])
            bet_type = int(form['type'])
            if flask_login.current_user.coins < amount:
                return "insufficient number of coins", 445
            try:
                new_bet = Bet(flask_login.current_user.id, amount, bet_type)
            except:
                return "impossible bet", 444
            flask_login.current_user.coins -= amount
            bets.append(new_bet)
            return "bet accepted", 200


@app.route('/bet/all', methods=['GET'])
def bet_all():
    if len(bets) > 0:
        all = "\n".join(f"<br>{str(bet)}</br>" for bet in bets)
        return all, 200
    return "Bets not found", 204


@app.route('/bet/all/json', methods=['GET'])
def bet_all_json():
    all_users = []
    all_types = []
    all_amounts = []
    if len(bets) > 0:
        for bet_i in bets:
            all_users.append(bet_i.user_id)
            all_types.append(bet_i.bet_type)
            all_amounts.append(bet_i.amount)

        data = {
            "users": all_users,
            "types": all_types,
            "amounts": all_amounts
        }

        return make_response(jsonify(data), 200)
    return "Bets not found", 204  # No content


@app.route('/start', methods=['GET'])
@flask_login.login_required
def start():
    global bets, result
    if not flask_login.current_user.is_croupier:
        return "You must be croupier", 403
    local_result = ""
    number = getRandomNum()
    local_result += f'The resulting number: {number}'
    all_users = []
    all_types = []
    all_amounts = []
    all_res = []
    for el in bets:
        all_users.append(el.user_id)
        all_types.append(el.bet_type)
        all_amounts.append(el.amount)
        if el.getResult(number):
            win_str = "WIN"
            all_res.append("WIN")
        else:
            win_str = "LOSE"
            all_res.append("LOSE")
        bet_str = str(el)
        local_result += f'\n<br>{bet_str} -- {win_str}</br>'

        prize = el.getPrize(number)

        if el.user_id in logged_in_users:
            logged_in_users[el.user_id].coins += prize
        else:
            users[el.user_id][1] += prize

    bets = []
    result = local_result
    global result_dict
    result_dict = {
        "number": number,
        "users": all_users,
        "types": all_types,
        "amounts": all_amounts,
        "result": all_res
    }
    return f'Successful. Number: {number}.', 200


@app.route('/result', methods=['GET'])
def results():
    return result, 200


@app.route('/result/json', methods=['GET'])
def results_json():
    if len(result_dict) == 0:
        return "No results yet", 204
    else:
        return make_response(jsonify(result_dict), 200)


bets = []
result = "No results yet"
result_dict = dict()
croupier = None
logged_in_users = dict()


def getRandomNum():
    return random.randint(0, 36)


if __name__ == "__main__":
    app.run(host=addr, port=5000)
