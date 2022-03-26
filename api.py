import requests as requests


def collect_data(fields, choices=None):
    data = {}
    if choices:
        for field in fields:
            if choices.get(field):
                print(f'Внимание! {field} может быть только: {choices[field]}')
            data[field] = input(f'Введите {field}: ')
    else:
        for field in fields:
            data[field] = input(f'Введите {field}: ')
    return data


class Api:
    def __init__(self):
        self.site = 'http://127.0.0.1:8000/api/v1/'
        self.headers = {}

    def get(self, path, obj_id=None):
        url = self.site + path
        if obj_id:
            url += f'{obj_id}/'
        try:
            response = requests.get(url, headers=self.headers)
            return response.json()
        except Exception as e:
            print(e)

    def post(self, path, data):
        url = self.site + path
        try:
            response = requests.post(url, json=data, headers=self.headers)
            return response.json()
        except Exception as e:
            print(e)

    def update(self, path, obj_id, data):
        url = self.site + path + obj_id + '/'
        try:
            response = requests.patch(url, json=data, headers=self.headers)
            return response.json()
        except Exception as e:
            print(e)

    def delete(self, path, obj_id):
        url = self.site + path + obj_id + '/'
        try:
            response = requests.delete(url, headers=self.headers)
            return response.json() if response.json() else response.status_code
        except Exception as e:
            print(e)

    class User:
        def __init__(self, api):
            self.api = api
            self.path = 'users/'
            self.fields = ['email', 'username', 'role', 'first_name',
                           'last_name', 'password']
            self.choices = {'role': ['company', 'subscriber']}

        def get_user(self):
            obj_id = input('Введите id пользователя или оставьте поле пустым '
                           'для получения всех: ')
            return self.api.get(self.path, obj_id)

        def post_user(self):
            data = collect_data(self.fields, self.choices)
            response = self.api.post(self.path, data)
            auth_data = self.api.Jwt(self.api).create(
                {
                    'username': data.get('username'),
                    'password': data.get('password')
                }
            )
            token = auth_data.get('access')
            self.api.headers['Authorization'] = f'Bearer {token}'
            response['refresh'] = auth_data.get('refresh')
            return response

        def update_user(self):
            obj_id = input('Введите id пользователя: ')
            return self.api.update(
                self.path, obj_id, collect_data(self.fields)
            )

        def delete_user(self):
            obj_id = input('Введите id пользователя: ')
            return self.api.delete(self.path, obj_id)

    class Meter:
        def __init__(self, api):
            self.api = api
            self.path = 'meters/'
            self.fields = ['user', 'meter_type']
            self.choices = {'meter_type': ['water', 'warm', 'electricity']}

        def get_meter(self):
            obj_id = input('Введите id счетчика или оставьте поле пустым '
                           'для получения всех: ')
            return self.api.get(self.path, obj_id)

        def post_meter(self):
            return self.api.post(
                self.path, collect_data(self.fields, self.choices)
            )

        def delete_meter(self):
            obj_id = input('Введите id счетчика: ')
            return self.api.delete(self.path, obj_id)

    class Reading:
        def __init__(self, api):
            self.api = api
            self.path = 'meters/'
            self.path2 = '/readings/'
            self.fields = ['value']

        def get_reading(self):
            meter_id = input('Введите id счетчика: ')
            obj_id = input('Введите id записи или оставьте поле пустым '
                           'для получения всех: ')
            return self.api.get(self.path + meter_id + self.path2, obj_id)

        def update_reading(self):
            meter_id = input('Введите id счетчика: ')
            obj_id = input('Введите id записи: ')
            return self.api.update(
                self.path + meter_id + self.path2,
                obj_id,
                collect_data(self.fields)
            )

        def delete_reading(self):
            meter_id = input('Введите id счетчика: ')
            obj_id = input('Введите id записи: ')
            return self.api.delete(self.path + meter_id + self.path2, obj_id)

    class Jwt:
        def __init__(self, api):
            self.api = api

        def create(self, in_user=None):
            if in_user:
                return self.api.post('jwt/create/', in_user)
            return self.api.post(
                'jwt/create/', collect_data(['username', 'password'])
            )

        def refresh(self):
            answer = self.api.post(
                'jwt/refresh/', collect_data(['refresh'])
            )
            token = answer.get('access')
            self.api.headers['Authorization'] = f'Bearer {token}'
            return answer

