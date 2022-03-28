import requests
import json


class Api:
    def __init__(self):
        self.domen = 'http://127.0.0.1:8000/api/v1/'
        self.token = None
        self.refresh_token = None
        self.headers = {}

    def some_get(self, url_part, uid=None):
        url = self.domen + url_part
        if uid:
            url += f'{uid}/'
        try:
            request = requests.get(url, headers=self.headers)
            return json.dumps({'status': request.status_code, 'data': request.json()}, indent=4)
        except Exception as e:
            print(e)

    def some_post(self, url_part, params):
        url = self.domen + url_part
        try:
            request = requests.post(url, json=params, headers=self.headers)
            return json.dumps({'status': request.status_code, 'data': request.json()}, indent=4)
        except Exception as e:
            print(e)

    def some_update(self, url_part, uid, params):
        url = self.domen + url_part + uid + '/'
        try:
            request = requests.patch(url, json=params, headers=self.headers)
            return json.dumps({'status': request.status_code, 'data': request.json()}, indent=4)
        except Exception as e:
            print(e)

    def some_delete(self, url_part, uid):
        url = self.domen + url_part + uid + '/'
        try:
            request = requests.delete(url, headers=self.headers)
            return json.dumps({'status': request.status_code, 'data': request.status_code}, indent=4)
        except Exception as e:
            print(e)

    def jwt_refresh(self, refresh):
        url = self.domen + 'jwt/refresh/'
        try:
            jwt_request = requests.post(
                url,
                json={'refresh': refresh},
                headers=self.headers
            )
            jwt = jwt_request.json()
            self.token = jwt.get('access')
            self.refresh_token = jwt.get('refresh')
            self.headers['Authorization'] = f'Bearer {self.token}'
            return json.dumps({'status': jwt_request.status_code}, indent=4)
        except Exception as e:
            print(e)

    def user_get(self, user_id=None):
        return self.some_get('users/', user_id)

    def user_post(self, params):
        url = self.domen + 'users/'
        jwt_url = self.domen + 'jwt/create/'
        try:
            request = requests.post(url, json=params)
            jwt_request = requests.post(jwt_url, json=params).json()
            self.token = jwt_request.get('access')
            self.refresh_token = jwt_request.get('refresh')
            self.headers['Authorization'] = f'Bearer {self.token}'
            return json.dumps({'status': request.status_code, 'data': request.json(), 'refresh': self.refresh_token}, indent=4)
        except Exception as e:
            print(e)

    def user_update(self, user_id, params):
        return self.some_update('users/', user_id, params)

    def user_delete(self, user_id):
        return self.some_delete('users/', user_id)

    def tasks_get(self, task_id=None):
        return self.some_get('tasks/', task_id)

    def tasks_post(self, params):
        return self.some_post('tasks/', params)

    def tasks_update(self, task_id, params):
        return self.some_update('tasks/', task_id, params)

    def tasks_delete(self, task_id):
        return self.some_delete('tasks/', task_id)

    def tasks_change_status(self, task_id, params):
        return self.some_post(f'tasks/{task_id}/status/', params)

    def offers_get(self, task_id, offer_id=None):
        return self.some_get(f'tasks/{task_id}/offers/', offer_id)

    def offers_post(self, task_id, params):
        return self.some_post(f'tasks/{task_id}/offers/', params)

    def offers_update(self, task_id, offer_id, params):
        return self.some_update(f'tasks/{task_id}/offers/', offer_id, params)

    def offers_delete(self, task_id, offer_id):
        return self.some_delete(f'tasks/{task_id}/offers/', offer_id)

    def offers_pick(self, task_id, offer_id):
        return self.some_post(
            f'tasks/{task_id}/offers/{offer_id}/pick/',
            params={}
        )
