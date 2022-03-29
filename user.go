package main

type User struct {
	Login        string
	Name         string
	Passwd       string
	CreatedTasks map[uint]struct{}
}

var Users = make(map[string]*User)
