package main

import (
	"encoding/json"
	"math/rand"
	"net/http"
)

type AddTaskRequest struct {
	Title       string `json:"title"`
	Description string `json:"description"`
}

type TaskHead struct {
	Title       string `json:"title"`
	Description string `json:"description"`
	Author      string `json:"author"`
	Executor    string `json:"executor"`
	Status      string `json:"status"`
}

const (
	FREE        = "free"
	IN_PROGRESS = "in_progress"
	DONE        = "done"
	CONFIRMED   = "confirmed"
)

type Task struct {
	TaskHead

	Author   *User
	Executor *User
}

var Tasks = make(map[uint]*Task)

func genID() uint {
	for {
		id := rand.Int()
		if _, ok := Tasks[uint(id)]; !ok {
			return uint(id)
		}
	}
}

func AddTask(head *TaskHead, user *User) uint {
	head.Author = user.Name
	head.Status = FREE
	task := &Task{
		TaskHead: *head,
		Author:   user,
	}

	id := genID()
	Tasks[id] = task
	user.CreatedTasks[id] = struct{}{}
	return id
}

func MakeTaskIDList() TaskIDList {
	var resp TaskIDList
	resp.Tasks = make([]uint, 0, len(Tasks))
	for k := range Tasks {
		resp.Tasks = append(resp.Tasks, k)
	}
	return resp
}

type TaskIDList struct {
	Tasks []uint `json:"tasks"`
}

func MyTasksHandler(w http.ResponseWriter, r *http.Request) {
	token := r.Header.Get("token")
	login := ParseToken(token)

	if login == "" {
		w.WriteHeader(http.StatusUnauthorized)
		return
	}

	resp := TaskIDList{make([]uint, 0, len(Users[login].CreatedTasks))}
	for k := range Users[login].CreatedTasks {
		resp.Tasks = append(resp.Tasks, k)
	}

	respJson, _ := json.Marshal(resp)
	w.Write(respJson)
}
