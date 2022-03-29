package main

import (
	"net/http"

	"github.com/gorilla/mux"
)

func main() {
	r := mux.NewRouter()

	r.Methods("POST").Path("/register").Handler(http.HandlerFunc(RegisterHandler))
	r.Methods("POST").Path("/login").Handler(http.HandlerFunc(LoginHandler))
	r.Methods("GET").Path("/tasks/{id:[0-9]+}").Handler(http.HandlerFunc(GetTaskHandler))
	r.Methods("PUT").Path("/tasks/{id:[0-9]+}").Handler(http.HandlerFunc(UpdateTaskStatusHandler))
	r.Methods("GET").Path("/tasks/my").Handler(http.HandlerFunc(MyTasksHandler))
	r.Methods("POST").Path("/tasks").Handler(http.HandlerFunc(AddTaskHandler))
	r.Methods("GET").Path("/tasks").Handler(http.HandlerFunc(AllTasksHandler))

	http.ListenAndServe(":25565", r)
}
