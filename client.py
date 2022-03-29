import requests
import json
import os
from commands import Command


ADDR = "http://peshkoff-nas.peshkoff.spb.ru:25565"


def update_status(token : str, task_id : int,  new_status : str) -> bool:
    header= {"token": token}
    req = json.dumps({"new_status": new_status})
    r = requests.put(ADDR+"/tasks/"+str(task_id), req, headers=header)
    return r.status_code == requests.codes.ok


def register(name : str, login : str, passwd : str) -> str:
    req = json.dumps({"name": name, "password": passwd, "login": login})
    r = requests.post(ADDR+"/register", req)
    return json.loads(r.content)["token"]


def login(login : str, passwd : str) -> str:
    req = json.dumps({"password": passwd, "login": login})
    r = requests.post(ADDR+"/login", req)
    if r.status_code == requests.codes.ok:
        resp = json.loads(r.content)
        return resp['token']
    else:
        return ''
        

def add_task(token : str, title : str, descr : str) -> int:
    header= {"token":token}
    req = json.dumps({"title":title, "description": descr})
    r = requests.post(ADDR + "/tasks", req, headers=header)
    return json.loads(r.content)["task_id"]


def get_my_tasks(token : str) -> list[int]:
    header = {"token": token}
    r = requests.get(ADDR + "/tasks/my",headers=header)
    return json.loads(r.content)["tasks"]


def get_task(task_id : int) -> str:
    return json.loads(requests.get(ADDR + "/tasks/" + str(task_id)).content)


def get_all_tasks() -> list[int]:
    r = requests.get(ADDR + "/tasks")
    return json.loads(r.content)["tasks"]


token = ''


def print_task(task_id : int):
    task = get_task(task_id)
    print('\ntask #', task_id)
    print('\ttitle:', task['title'])
    print('\tdescription:', task['description'])
    print('\tauthor:', task['author'])
    print('\texecutor:', task['executor'])
    print('\tstatus:', task['status'])
    print('\n')


while True:
    command = input()
    if command == 'exit':
        os._exit(1)
        
    elif command == Command.REGISTER.value:
        token = register(input('name: '), input('login: '), input('password: '))
        if token == '':
            print('error')
        
    elif command == Command.LOGIN.value:
        token = login(input('login: '), input('password: '))
        if token == '':
            print('error')
        
    elif command == Command.ADD_TASK.value:
        if token == '':
            print('you are not logged in')
            continue
        task_id = add_task(token, input('title: '), input('description: '))
        print_task(task_id)

    elif command == Command.MY_TASKS.value:
        if token == '':
            print('you are not logged in')
            continue
        my_tasks = get_my_tasks(token)

        for task in my_tasks:
            print_task(task)
        
    elif command == Command.ALL_TASKS.value:
        tasks = get_all_tasks()
        for task in tasks:
            print_task(task)
        
    elif command == Command.UPDATE_TASK.value:
        if token == '':
            print('you are not logged in')
            continue
        task_id = int(input('task_id: '))
        task = get_task(task_id)
        print('avaliable statuses: free, in_progress, done, confirmed')
        if update_status(token, task_id, input('new status: ')):
            print('Updated!')
            print_task(task_id)
        else:
            print('forbidden')

    elif command == Command.LOGOUT.value:
        token = ''
        print("Logged out")

    else:
        print('unknown command')
