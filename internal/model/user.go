package model

type User struct {
	Username  string   `json:"username"`
	Money     float64  `json:"money"`
	Purchases []string `json:"purchases"`
}
