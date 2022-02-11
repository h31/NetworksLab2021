package model

type Bet struct {
	PlayerName string  `json:"name"`
	Type       string  `json:"type"`
	Amount     float64 `json:"amount"`
	Status     string  `json:"status"`
}
type Player struct {
	Name    string  `json:"name"`
	Balance float64 `json:"balance"`
	Bet     float64 `json:"bet"`
}
type Croupier struct {
	Name    string `json:"name"`
	BetList []Bet  `json:"bets"`
	Start   bool   `json:"start"`
}
