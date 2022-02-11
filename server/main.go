package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/gnatunstyles/roulette/service"
)

var (
	p service.GameService = service.New()
)

func main() {
	s := gin.Default()

	s.GET("/players", func(c *gin.Context) {
		c.JSON(http.StatusOK, p.FindPlayers())
	})
	//выдача клиенту ставок
	s.GET("/bets", func(c *gin.Context) {
		c.JSON(http.StatusOK, p.FindBets())
	})
	s.GET("/croupier", func(c *gin.Context) {
		c.JSON(http.StatusOK, p.FindCroupier())
	})

	s.POST("/croupier", func(c *gin.Context) {
		c.JSON(http.StatusOK, p.NewCroupier(c))
	})
	s.POST("/players", func(c *gin.Context) {
		c.JSON(http.StatusOK, p.NewPlayer(c))
	})
	s.POST("/bets/:name/:type/:amount", func(c *gin.Context) {
		c.JSON(http.StatusOK, p.MakeBet(c))

	})
	s.POST("/start", func(c *gin.Context) {
		c.JSON(http.StatusOK, p.StartGame(c))
	})
	s.POST("/players/:name/:balance", func(c *gin.Context) {
		c.JSON(http.StatusOK, p.GetPrize(c))
	})
	s.Run(":8080")
}
