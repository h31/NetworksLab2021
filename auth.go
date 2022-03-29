package main

import (
	"errors"
	"time"

	"github.com/dgrijalva/jwt-go/v4"
)

const (
	signingString = "dwqdqwdwefwrercsdc3q4g3g41NaeTarnadwM4N4MnNffAFrNql7ocA8fIpw8ZEY2cDM1GF1vYOUbIGJgfe"
)

type Claims struct {
	jwt.StandardClaims
	Login string `json:"username"`
}

func GenerateToken(login string) string {
	token := jwt.NewWithClaims(jwt.SigningMethodHS256, &Claims{
		StandardClaims: jwt.StandardClaims{
			IssuedAt:  jwt.At(time.Now()),
			ExpiresAt: jwt.At(time.Now().Add(3 * time.Hour)),
		},
		Login: login,
	})
	str, _ := token.SignedString([]byte(signingString))
	return str
}

func ParseToken(tokenStr string) string {
	claims := &Claims{}
	token, err := jwt.ParseWithClaims(tokenStr, claims, func(token *jwt.Token) (interface{}, error) {
		if token.Method == jwt.SigningMethodHS256 {
			return []byte(signingString), nil
		} else {
			return nil, errors.New("")
		}
	})
	if err != nil {
		return ""
	}

	var ok bool
	if claims, ok = token.Claims.(*Claims); ok && token.Valid {
		return claims.Login
	}
	return ""
}
