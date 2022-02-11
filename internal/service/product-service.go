package service

import (
	"net/http"

	"github.com/gin-gonic/gin"
	"github.com/gnatunstyles/shop/internal/model"
)

type ProductService interface {
	Save(ctx *gin.Context) model.Product
	FindAll() []model.Product
	GetProductByName(ctx *gin.Context)
	RemoveProductByName(ctx *gin.Context)
}

type productService struct {
	products []model.Product
}

func NewProduct() ProductService {
	return &productService{
		products: []model.Product{
			{
				Name:  "Orange",
				Price: 30},
			{
				Name:  "Carrot",
				Price: 10},
		},
	}
}
func (p *productService) GetProductByName(ctx *gin.Context) {
	name := ctx.Param("name")
	for _, v := range p.products {
		if name == v.Name {
			ctx.IndentedJSON(http.StatusOK, v)
			return
		}
	}
	ctx.IndentedJSON(http.StatusNotFound, gin.H{"message": "product not found"})
}

func (p *productService) RemoveProductByName(ctx *gin.Context) {
	name := ctx.Param("name")
	for i := range p.products {
		if name == p.products[i].Name {
			p.products = append(p.products[:i], p.products[i+1:]...)
			ctx.IndentedJSON(http.StatusOK, p.products)
			return
		}
	}
	ctx.IndentedJSON(http.StatusNotFound, gin.H{"message": "product not found"})
}

func (p *productService) Save(ctx *gin.Context) model.Product {
	var prod model.Product
	if err := ctx.BindJSON(&prod); err != nil {
		ctx.IndentedJSON(http.StatusBadRequest, "bad request")
		return model.Product{}
	}
	p.products = append(p.products, prod)
	return prod
}

func (p *productService) FindAll() []model.Product {
	return p.products
}
