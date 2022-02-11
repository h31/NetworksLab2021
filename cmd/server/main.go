package main

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/gnatunstyles/shop/internal/service"
)

var (
	productService service.ProductService = service.NewProduct()
	userService    service.UserService    = service.NewUser()
)

func main() {
	server := gin.Default()

	//test endpoint
	server.GET("/ping", func(c *gin.Context) {
		c.JSON(http.StatusOK, gin.H{
			"message": "OK",
		})
	})

	//user endpoints
	server.GET("/users", func(ctx *gin.Context) {
		ctx.JSON(http.StatusOK, userService.FindAll())
	})

	server.POST("/users", func(ctx *gin.Context) {
		ctx.JSON(http.StatusOK, userService.Save(ctx))
	})
	server.POST("/users/:username/:money/:good", userService.Purchase)

	//product endpoints
	server.GET("/products", func(ctx *gin.Context) {
		ctx.JSON(http.StatusOK, productService.FindAll())
	})
	server.POST("/products", func(ctx *gin.Context) {
		ctx.JSON(http.StatusOK, productService.Save(ctx))
	})
	server.GET("/products/:name", productService.GetProductByName)

	server.DELETE("/products/:name", productService.RemoveProductByName)

	//run server
	server.Run(":8080")
}
