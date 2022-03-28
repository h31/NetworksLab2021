import requests as requests

import phrases as p


def collect_data(fields, choices=None):
    data = {}
    if choices:
        for field in fields:
            if choices.get(field):
                print(f'Внимание! {field} может быть только: {choices[field]}')
            value = input(f'Введите {field}: ')
            if value:
                data[field] = value
    else:
        for field in fields:
            value = input(f'Введите {field}: ')
            if value:
                data[field] = value
    return data


class Api:
    def __init__(self):
        self.domain = p.site
        self.headers = {}

    def get(self, path, oid=None):
        url = self.domain + path
        if oid:
            url += f'{oid}/'
        try:
            response = requests.get(url, headers=self.headers)
            return response.json()
        except Exception as e:
            print(e)

    def post(self, path, data):
        url = self.domain + path
        try:
            response = requests.post(url, json=data, headers=self.headers)
            return response.json()
        except Exception as e:
            print(e)

    def update(self, path, oid, data):
        url = self.domain + path + oid + '/'
        try:
            response = requests.patch(url, json=data, headers=self.headers)
            return response.json()
        except Exception as e:
            print(e)

    def delete(self, path, oid):
        url = self.domain + path + oid + '/'
        try:
            response = requests.delete(url, headers=self.headers)
            return response.json() if response.json() else response.status_code
        except Exception as e:
            print(e)

    class User:
        def __init__(self, api):
            self.api = api
            self.path = 'users/'
            self.fields = ['email', 'username', 'first_name',
                           'last_name', 'password']

        def get_user(self):
            oid = input(p.not_required_parking_id)
            return self.api.get(self.path, oid)

        def post_user(self):
            data = collect_data(self.fields)
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
            oid = input(p.required_user_id)
            return self.api.update(
                self.path, oid, collect_data(self.fields)
            )

        def delete_user(self):
            oid = input(p.required_user_id)
            return self.api.delete(self.path, oid)

    class Parking:
        def __init__(self, api):
            self.api = api
            self.path = 'parking/'
            self.fields = ['name', 'address']

        def get_parking(self):
            oid = input(p.not_required_parking_id)
            return self.api.get(self.path, oid)

        def post_parking(self):
            return self.api.post(
                self.path, collect_data(self.fields)
            )

        def update_parking(self):
            oid = input(p.required_parking_id)
            return self.api.update(self.path, oid, collect_data(self.fields))

        def delete_parking(self):
            oid = input(p.required_parking_id)
            return self.api.delete(self.path, oid)

    class Record:
        def __init__(self, api):
            self.api = api
            self.path = 'parking/'
            self.path2 = '/record/'
            self.fields = ['state_number']

        def get_record(self):
            parking_id = input(p.required_parking_id)
            oid = input(p.not_required_record_id)
            return self.api.get(self.path + parking_id + self.path2, oid)

        def post_record(self):
            parking_id = input(p.required_parking_id)
            return self.api.post(self.path + parking_id + self.path2, collect_data(self.fields))

        def update_record(self):
            parking_id = input(p.required_parking_id)
            oid = input(p.required_record_id)
            return self.api.update(
                self.path + parking_id + self.path2,
                oid,
                collect_data(self.fields)
            )

        def delete_record(self):
            parking_id = input(p.required_parking_id)
            oid = input(p.required_record_id)
            return self.api.delete(self.path + parking_id + self.path2, oid)

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
