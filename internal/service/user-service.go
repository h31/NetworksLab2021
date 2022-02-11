package service

import (
	"log"
	"net/http"
	"strconv"

	"github.com/gin-gonic/gin"
	"github.com/gnatunstyles/shop/internal/model"
)

type UserService interface {
	Save(ctx *gin.Context) model.User
	FindAll() []model.User
	Purchase(ctx *gin.Context)
}

type userService struct {
	users []model.User
}

func NewUser() UserService {
	return &userService{}
}

func (user *userService) Save(ctx *gin.Context) model.User {
	var newUser model.User
	if err := ctx.BindJSON(&newUser); err != nil {
		ctx.IndentedJSON(http.StatusBadRequest, "bad request")
		return model.User{}
	}
	for _, v := range user.users {
		if v.Username == newUser.Username {
			log.Println("Error. User with this name already exists.")
			return model.User{}
		}
	}
	user.users = append(user.users, newUser)
	return newUser
}

func (user *userService) Purchase(ctx *gin.Context) {
	name := ctx.Param("username")
	money := ctx.Param("money")
	good := ctx.Param("good")
	for i := range user.users {
		if user.users[i].Username == name {
			m, _ := strconv.ParseFloat(money, 64)
			user.users[i].Money = m
			user.users[i].Purchases = append(user.users[i].Purchases, good)
			ctx.IndentedJSON(http.StatusOK, user.users)
			return
		}
	}
}

func (user *userService) FindAll() []model.User {
	return user.users
}
