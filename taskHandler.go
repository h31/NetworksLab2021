package main

import (
	"encoding/json"
	"net/http"
	"strconv"

	"github.com/gorilla/mux"
)

type addTaskResponse struct {
	ID uint `json:"task_id"`
}

func AddTaskHandler(w http.ResponseWriter, r *http.Request) {
	var head TaskHead

	token := r.Header.Get("token")
	login := ParseToken(token)

	if login == "" {
		w.WriteHeader(http.StatusUnauthorized)
		return
	}

	if json.NewDecoder(r.Body).Decode(&head) != nil {
		w.WriteHeader(http.StatusBadRequest)
		return
	}

	id := AddTask(&head, Users[login])

	addTaskResponse := addTaskResponse{id}
	respJson, _ := json.Marshal(addTaskResponse)
	w.Write(respJson)
}

func GetTaskHandler(w http.ResponseWriter, r *http.Request) {
	id, _ := strconv.Atoi(mux.Vars(r)["id"])

	task, ok := Tasks[uint(id)]

	if !ok {
		w.WriteHeader(http.StatusNotFound)
		return
	}
	respJson, _ := json.Marshal(task.TaskHead)
	w.Write(respJson)
}

func AllTasksHandler(w http.ResponseWriter, _ *http.Request) {
	respJson, _ := json.Marshal(MakeTaskIDList())
	w.Write(respJson)
}

type updateStatusRequest struct {
	NewStatus string `json:"new_status"`
}

func UpdateTaskStatusHandler(w http.ResponseWriter, r *http.Request) {
	var req updateStatusRequest

	token := r.Header.Get("token")
	login := ParseToken(token)

	id, _ := strconv.Atoi(mux.Vars(r)["id"])
	task, ok := Tasks[uint(id)]

	if login == "" {
		w.WriteHeader(http.StatusUnauthorized)
	}

	if !ok || json.NewDecoder(r.Body).Decode(&req) != nil {
		w.WriteHeader(http.StatusBadRequest)
		return
	}
	user := Users[login]

	//very simple logic
	//every task goes through several steps
	// FREE -> IN_PROGRESS -> DONE => CONFIRMED
	// -> - only executor can change
	// => - only author can change
	if user == task.Author { //update from author
		if task.Status == DONE && req.NewStatus == CONFIRMED {
			task.Status = CONFIRMED
			return
		} //else just iqnore
	} else { // update from executor
		if task.Status == FREE && req.NewStatus == IN_PROGRESS {
			task.TaskHead.Executor = user.Name
			task.Executor = user
			task.Status = IN_PROGRESS
			return
		} else if task.Status == IN_PROGRESS && req.NewStatus == DONE {
			task.Status = DONE
			return
		}
	}
	w.WriteHeader(http.StatusForbidden)
}
