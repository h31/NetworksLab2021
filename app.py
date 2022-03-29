from flask import Flask, render_template, request, flash, redirect, session, abort, make_response
from flask_sqlalchemy import SQLAlchemy
from server_answers import AuthAnswers as au_ans
from server_answers import CreateAnswers as cr_ans
from server_answers import PersAccAnswers as pa_ans

app = Flask(__name__)
app.config['SECRET_KEY'] = 'secretkey'
app.config['TEMPLATES_AUTO_RELOAD'] = True
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///eps.db'
db = SQLAlchemy(app)


class Person(db.Model):
    id = db.Column(db.INTEGER, primary_key=True)
    name = db.Column(db.String, unique=True)
    password = db.Column(db.String, primary_key=False)
    count = db.Column(db.Integer, default=100)

    def __init__(self, name=None, password=None):
        self.name = name
        self.password = password

    def __repr__(self):
        return self.name

    def get_name(self):
        return self.name

    def get_pass(self):
        return f"{self.password}"


@app.route('/')
def main_page():  # put application's code here
    return render_template('main_page.html', title='Система электронных платежей', header='Главная страница')


def get_user_data(req):
    login = req.form['login'] if 'login' in req.form else req.json['login']
    pswd = req.form['pass'] if 'pass' in req.form else req.json['pass']
    db_data = Person.query.filter_by(name=login).first()
    return db_data, login, pswd


def auth_person(req):
    if 'userLogged' in session:
        return au_ans.ALREADY_LOGGED.value
    else:
        if req.method == "GET":
            return au_ans.GET_INFO.value
        elif req.method == "POST":
            db_data, login, pswd = get_user_data(req)
            if db_data is None:
                return au_ans.USER_DOESNT_EXIST.value
            elif pswd != db_data.get_pass():
                return au_ans.INCORRECT_PSWD.value
            else:
                session['userLogged'] = login
                return au_ans.SUCCESS.value


@app.route('/api/authorization', methods=["GET", "POST"])
def api_authorization():
    print(request)
    ans = auth_person(request)
    if ans == au_ans.ALREADY_LOGGED.value:
        return make_response({'ans': "Already logged", 'name': session['userLogged']}, 200)
    elif ans == au_ans.GET_INFO.value:
        return make_response({'ans': 'You haven\'t logged yet'}, 403)
    elif ans == au_ans.USER_DOESNT_EXIST.value:
        return make_response({'ans': 'This user doesn\'t exist'}, 401)
    elif ans == au_ans.INCORRECT_PSWD.value:
        return make_response({'ans': 'Password is incorrect'}, 401)
    elif ans == au_ans.SUCCESS.value:
        return make_response({"ans": "success"}, 200)


@app.route('/authorization', methods=["POST", "GET"])
def authorization():
    ans = auth_person(request, 0)
    if ans == au_ans.GET_INFO.value:
        return render_template('authorization.html', title='Вход в систему', header='Вход в систему')
    elif ans == au_ans.ALREADY_LOGGED.value:
        return redirect(f"/lk/<{session['userLogged']}>")
    elif ans == au_ans.USER_DOESNT_EXIST.value:
        flash('Такого пользователя не существует')
        return redirect('/authorization')
    elif ans == au_ans.INCORRECT_PSWD.value:
        flash('Введен неверный пароль')
        return redirect('/authorization')
    elif ans == au_ans.SUCCESS.value:
        return redirect(f"/lk/<{session['userLogged']}>")


def create_person(req):
    val = None
    if 'login' not in req.form or 'pass' not in req.form:
        val = cr_ans.MISSED_VALUE.value
    if 'login'not in req.json or 'pass' not in req.json:
        return val
    else:
        login = req.form['login'] if 'login' in req.form else req.json['login']
        pswd = req.form['pass'] if 'pass' in req.form else req.json['pass']
        pers = Person(name=login, password=pswd)
        print(f"created {pers.id, pers.name, pers.password, pers.count}")
        session['userLogged'] = login
        try:
            db.session.add(pers)
            db.session.commit()
            return 0
        except Exception as e:
            print(f"Something went wrong, {e}")
            del session['userLogged']
            return 1


@app.route('/api/registration', methods=["POST", "GET"])
def api_registration():
    if 'userLogged' in session:
        return make_response({'ans': f"Already logged as {session['userLogged']}"}, 205)
    else:
        if request.method == "GET":
            return make_response({'ans': "To register send a post-request to this url with login and pass"}, 200)
        elif request.method == "POST":
            ans = create_person(request)
            if ans == cr_ans.MISSED_VALUE.value:
                return make_response({'ans': "Missed a required value"}, 400)
            elif ans == cr_ans.SUCCESSFUL_CREATED.value:
                return make_response({'ans': "Successfully registered"}, 200)
            else:
                return make_response({'ans': "User with this nickname has already registered"}, 100)


@app.route('/registration', methods=["POST", "GET"])
def registration():
    if request.method == "GET":
        return render_template('registration.html', title='Регистрация в системе',
                               header='Создание новой учетной записи')
    elif request.method == "POST":
        ans = create_person(request)
        if ans == cr_ans.SUCCESSFUL_CREATED.value:
            return redirect(f"/lk/<{request.form['login']}>")
        elif ans == cr_ans.USER_EXIST.value:
            flash("Пользователь с таким именем уже существует")
            return redirect('/registration')


@app.route('/lk')
def pa_redirect():
    if 'userLogged' not in session:
        return redirect('/authorization')
    else:
        return redirect(f"/lk/<{session['userLogged']}>")


def manage_account(req, username):
    if 'userLogged' not in session or session['userLogged'] != username:
        return pa_ans.NO_ACCESS.value
    elif req.method == "GET":
        return pa_ans.GET_INFO.value
    elif 'exit' in request.form.to_dict():
        del session['userLogged']
        return pa_ans.EXIT.value
    else:
        payee = request.form['username'] if 'username' in request.form else request.json['username']
        amount = request.form['amount'] if 'amount' in request.form else request.json['amount']
        amount = int(amount)
        getter = Person.query.filter_by(name=payee).first()
        payer = Person.query.filter_by(name=username).first()
        if getter is None:
            return pa_ans.NO_GETTER.value
        elif payer.count < amount:
            return pa_ans.NO_MONEY.value
        elif amount < 0:
            return pa_ans.NEGATIVE_SUM.value
        else:
            getter.count += amount
            payer.count -= amount
            db.session.commit()
            return pa_ans.SUCCESS.value


@app.route('/api/lk', methods=["POST", "GET"])
def api_pa():
    if 'userLogged' not in session:
        return make_response({'ans': "You should login to the system"}, 403)
    else:
        username = session['userLogged']
        ans = manage_account(request, username)
        if ans == pa_ans.GET_INFO.value:
            cnt = Person.query.filter_by(name=username).first().count
            return make_response({'ans': f"You have {cnt}"}, 200)
        elif ans == pa_ans.NO_MONEY.value:
            return make_response({'ans': "You have no money to do this transfer"}, 400)
        elif ans == pa_ans.NO_GETTER.value:
            return make_response({'ans': "No user with mentioned nickname"}, 400)
        elif ans == pa_ans.EXIT.value:
            return make_response({'ans': "Successfully logout"}, 200)
        elif ans == pa_ans.NEGATIVE_SUM.value:
            return make_response({'ans': "You can't transfer negative amount of money"}, 400)
        elif ans == pa_ans.SUCCESS.value:
            cnt = Person.query.filter_by(name=username).first().count
            return make_response({'ans': f"Successfully transferred. Now you have {cnt}"}, 200)


@app.route('/lk/<username>', methods=["POST", "GET"])
def personal_account(username):
    un = username[1:-1]
    ans = manage_account(request, un)
    if ans == pa_ans.NO_ACCESS.value:
        abort(401)
    elif ans == pa_ans.SUCCESS.value:
        flash("Перевод прошел успешно")
    elif ans == pa_ans.NO_GETTER.value:
        flash("Пользователя с таким именем не существует")
    elif ans == pa_ans.NO_MONEY.value:
        flash("На счету недостаточно средств")
    elif ans == pa_ans.EXIT.value:
        return redirect("/")
    elif ans == pa_ans.NEGATIVE_SUM.value:
        flash("Вы не можете перевести отрицательную сумму")

    cnt = Person.query.filter_by(name=un).first().count
    return render_template('personal_account.html', title='Личный кабинет', header='Личный кабинет',
                            username=un, count=cnt)


@app.errorhandler(404)
def pageNotFound(error):
    return render_template("error404.html", title='Страница не найдена')


if __name__ == '__main__':
    app.run()
