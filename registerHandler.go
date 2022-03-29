package main

import (
	"encoding/json"
	"net/http"
)

type LoginRequest struct {
	Login  string `json:"login"`
	Passwd string `json:"password"`
}

type RegisterRequest struct {
	LoginRequest
	Name string `json:"name"`
}

type TokenResponse struct {
	Token string `json:"token"`
}

func RegisterHandler(w http.ResponseWriter, r *http.Request) {
	var req RegisterRequest

	if json.NewDecoder(r.Body).Decode(&req) != nil {
		w.WriteHeader(http.StatusBadRequest)
		return
	}

	for _, user := range Users {
		if user.Login == req.Login {
			w.WriteHeader(http.StatusConflict)
		}
	}

	Users[req.Login] = &User{
		Name:         req.Name,
		Passwd:       req.Passwd,
		Login:        req.Login,
		CreatedTasks: make(map[uint]struct{}),
	}

	resp := TokenResponse{Token: GenerateToken(req.Login)}
	respJson, _ := json.Marshal(resp)
	w.Write(respJson)
}

func LoginHandler(w http.ResponseWriter, r *http.Request) {
	var loginReq LoginRequest

	if json.NewDecoder(r.Body).Decode(&loginReq) != nil {
		w.WriteHeader(http.StatusBadRequest)
		return
	}
	user, ok := Users[loginReq.Login]
	if ok && user.Passwd == loginReq.Passwd {
		resp := TokenResponse{Token: GenerateToken(user.Login)}
		respJson, _ := json.Marshal(resp)
		w.Write(respJson)
		return
	}
	w.WriteHeader(http.StatusForbidden)
}
