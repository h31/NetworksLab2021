import random
import flask_login
import flask

from flask import jsonify, make_response
from Bet import Bet
from User import User

app = flask.Flask(__name__)
app.secret_key = 'qwerty'

login_manager = flask_login.LoginManager()
login_manager.init_app(app)

addr = '192.168.0.102'

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
    login = request.form.get('login')
    if login not in logged_in_users:
        return

    try:
        is_croupier = request.form.get('croupier') == 'on'
    except:
        is_croupier = False

    user = User(users[login][1], is_croupier)
    user.id = login

    return user


@app.route('/')
def redirect():
    if flask_login.current_user.is_authenticated:
        return flask.redirect(flask.url_for('protected'))
    return flask.redirect(flask.url_for('login'))


@app.route('/login', methods=['GET', 'POST'])
def login():

    if flask.request.method == 'GET':
        if flask_login.current_user.is_authenticated:
            print(flask_login.current_user.id)
            return flask.redirect(flask.url_for('protected'))
        return '''
               <form action='login' method='POST'>
                <input type='text' name='login' id='login' placeholder='login'/>
                <input type='password' name='password' id='password' placeholder='password'/>
                <input type='checkbox' name='croupier' id='croupier' placeholder='croupier'/>
                Login as croupier
                <input type='submit' name='submit'/>
               </form>
               '''

    if flask_login.current_user.is_authenticated:
        return "You already login", 441  # ERROR 441 - try login twice
    try:
        local_login = flask.request.form['login']
        password = flask.request.form['password']
    except:
        return "You must specify the login and password", 442  # ERROR 442 - Some field is not filled

    if local_login not in users:
        return bad_login(443)  # ERROR 443 - Incorrect login or password

    try:
        is_croupier = flask.request.form['croupier'] == 'on'
    except:
        is_croupier = False

    if password == users[local_login][0]['password']:
        user = User(users[local_login][1], is_croupier)
        user.id = local_login

        global croupier
        if is_croupier:
            if croupier is None:
                croupier = user
            else:
                return 'the croupier already exists', 435


        logged_in_users[local_login] = user
        flask_login.login_user(user)
        return flask.redirect(flask.url_for('protected'))

    return bad_login(443)


def bad_login(code):
    return f'''
        BAD LOGIN
        <a href="http://{addr}:5000/login">
           <input type="button" value="Try again" />
        </a>
    ''', code


@app.route('/protected')
@flask_login.login_required
def protected():
    doc = f'''
        <br>
        Logged in as: {flask_login.current_user.id} \n
        </br><br>
        Your coins: {flask_login.current_user.coins}
        </br>
    '''

    return doc


@app.route('/protected/json')
@flask_login.login_required
def protected_json():
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

    try:
        logged_in_users.pop(flask_login.current_user.id)
    except:
        return "something broken", 420

    flask_login.logout_user()
    return 'Logged out'


@app.route('/bet', methods=['GET', 'POST'])
@flask_login.login_required
def bet():
    if flask_login.current_user.is_croupier:
        return "You are croupier. You can't do bets", 403
    if flask.request.method == 'GET':
        return f'''
               <form action='bet' method='POST'>
                Type:<input type='number' min='-2' max='36' name='type' id='type' placeholder='type'/>
                Bet amount:<input type='number' min='1' name='amount' id='amount' placeholder='amount'/>
                <input type='submit' name='submit'/>
               </form>
               '''

    try:
        amount = int(flask.request.form['amount'])
        type = int(flask.request.form['type'])
    except:
        return "you must specify the correct type and amount", 444

    if flask_login.current_user.coins < amount:
        return "insufficient number of coins", 445

    try:
        new_bet = Bet(flask_login.current_user.id, amount, type)
    except:
        return "impossible bet", 444

    flask_login.current_user.coins -= amount
    bets.append(new_bet)
    return "bet accepted"


@app.route('/bet/all', methods=['GET'])
def bet_all():
    if len(bets) > 0:
        all = "\n".join(f'''<br>{str(bet)}</br>''' for bet in bets)
        return all
    return "Not find bets"


@app.route('/start', methods=['GET'])
@flask_login.login_required
def start():
    global bets, result
    if not flask_login.current_user.is_croupier:
        return "You must be croupier", 403
    local_result = ""
    number = getRandomNum()
    local_result += f'The resulting number: {number}'
    for el in bets:
        if el.getResult(number):
            win_str = "WIN"
        else:
            win_str = "LOSE"
        bet_str = str(el)
        local_result += f'\n<br>{bet_str} -- {win_str}</br>'

        prize = el.getPrize(number)
        try:
            logged_in_users[el.user_id].coins += prize
        except:
            users[el.user_id][1] += prize

    bets = []
    result = local_result
    return f'Successful. Number: {number}.', 200


@app.route('/result', methods=['GET'])
def results():
    return result


bets = []
result = "No results yet"
croupier = None
logged_in_users = dict()


def getRandomNum():
    return random.randint(0, 36)


if __name__ == "__main__":
    app.run(host=addr, port=5000)

# number = getRandomNum()
# for bet in bets:
#     print(bet.getPrize(number))
