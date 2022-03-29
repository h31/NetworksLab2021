import text

text.init()


def jwt(client_api):
    api = client_api
    answer = api.jwt_refresh(input(text.refresh))
    print(answer)