package service

import (
	"crypto/rand"
	"log"
	"math/big"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/gnatunstyles/roulette/model"
)

type GameService interface {
	NewPlayer(c *gin.Context) model.Player
	NewCroupier(c *gin.Context) model.Croupier
	MakeBet(c *gin.Context) []model.Bet
	FindPlayers() []model.Player
	FindBets() []model.Bet
	FindCroupier() model.Croupier
	StartGame(c *gin.Context) []model.Bet
	GetPrize(c *gin.Context) model.Player
}

type gameService struct {
	players  []model.Player
	bets     []model.Bet
	croupier model.Croupier
}

func New() GameService {
	return &gameService{}
}

func (p *gameService) FindCroupier() model.Croupier {
	return p.croupier
}

func (p *gameService) FindPlayers() []model.Player {
	return p.players
}
func (p *gameService) FindBets() []model.Bet {
	return p.bets
}

func (p *gameService) NewPlayer(c *gin.Context) model.Player {
	var newPlayer model.Player
	if err := c.BindJSON(&newPlayer); err != nil {
		c.IndentedJSON(http.StatusBadRequest, "bad request")
		return model.Player{}
	}
	for _, v := range p.players {
		if v.Name == newPlayer.Name {
			log.Println("Error. Player with this name already exists.")
			return model.Player{}
		}
	}
	p.players = append(p.players, newPlayer)
	return newPlayer
}

func (p *gameService) NewCroupier(c *gin.Context) model.Croupier {
	if p.croupier.Name == "" {
		var newCroupier model.Croupier
		if err := c.BindJSON(&newCroupier); err != nil {
			c.IndentedJSON(http.StatusBadRequest, "bad request")
			return model.Croupier{}
		}
		p.croupier = newCroupier
		return newCroupier
	} else {
		log.Print("Error. Croupier name is empty or croupier already exists.")
		return model.Croupier{}
	}
}

func (p *gameService) MakeBet(c *gin.Context) []model.Bet {
	var newBet model.Bet
	newBet.PlayerName = c.Param("name")
	newBet.Type = c.Param("type")
	newBet.Amount, _ = strconv.ParseFloat(c.Param("amount"), 64)
	newBet.Status = ""
	for _, v := range p.bets {
		if v.PlayerName == newBet.PlayerName {
			log.Println("Error. This player already made the bet!")
			return p.bets
		}
	}
	for i := range p.players {
		if p.players[i].Name == newBet.PlayerName {
			p.players[i].Balance -= newBet.Amount
		}
	}
	p.bets = append(p.bets, newBet)
	p.croupier.BetList = append(p.croupier.BetList, newBet)
	return p.bets
}

func (p *gameService) StartGame(c *gin.Context) []model.Bet {
	rNum, _ := rand.Int(rand.Reader, big.NewInt(37))
	result := int(rNum.Int64())
	log.Print(result)
	for i := range p.bets {
		activeBet, err := strconv.Atoi(p.bets[i].Type)
		if err == nil {
			if activeBet == result {
				p.bets[i].Amount *= 36
				p.bets[i].Status = "win"
			} else {
				p.bets[i].Amount = 0
				p.bets[i].Status = "lose"
			}
		} else if (p.bets[i].Type == "even" && result%2 == 0) ||
			(p.bets[i].Type == "odd" && result%2 == 1) {
			p.bets[i].Amount *= 2
			p.bets[i].Status = "win"
		} else {
			p.bets[i].Amount = 0
			p.bets[i].Status = "lose"
		}
	}
	return p.bets
}

func (p *gameService) GetPrize(c *gin.Context) model.Player {
	var player model.Player
	player.Name = c.Param("name")
	player.Balance, _ = strconv.ParseFloat(c.Param("balance"), 64)

	for _, bet := range p.bets {
		if bet.PlayerName == player.Name {
			if bet.Amount > 0 {
				log.Printf("It seems, that %s won some money! Congratulations!", player.Name)
				player.Balance += bet.Amount
			} else {
				log.Printf("%s lost his money!", player.Name)
			}
			for i := range p.players {
				if p.players[i].Name == player.Name {
					p.players[i].Balance = player.Balance
					return p.players[i]
				}
			}
		}
	}
	log.Print("You need to make a bet to take your prize! Come back later!")
	return player
}
