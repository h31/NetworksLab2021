package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strconv"
	"syscall"

	"github.com/gnatunstyles/shop/internal/model"
)

func main() {
	addr := map[string]string{
		"prod_list": "http://localhost:8080/products",
		"user_list": "http://localhost:8080/users",
	}
	var user model.User

	fmt.Println("Enter your name and amount of money:")
	fmt.Scanln(&user.Username, &user.Money)

	userJson, _ := json.Marshal(user)
	_, err := http.Get("http://localhost:8080/ping")
	if err != nil {
		log.Fatal(err)
		return
	}

	fmt.Println("Welcome to the online shop! Print -help to see the list of actions.")
	client := &http.Client{}

	flag := false
	var activeUsers []model.User
	resp, _ := http.Get(addr["user_list"])
	body, _ := ioutil.ReadAll(resp.Body)
	defer resp.Body.Close()

	json.Unmarshal(body, &activeUsers)

	for _, v := range activeUsers {
		if v.Username == user.Username {
			flag = true
			user.Money = v.Money
			user.Purchases = v.Purchases
			break
		}
	}

	if flag {
		fmt.Printf("Welcome back, %s!", user.Username)

	} else {
		_, err = http.Post(addr["user_list"], "application/json", bytes.NewBuffer(userJson))
		if err != nil {
			log.Fatal(err)
			return
		}
	}

	for {
		var op string
		fmt.Scanln(&op)
		switch op {

		case "-help":
			fmt.Printf("Command list:\n\n -help : usable commands \n\n -buy : buy the product \n\n -show : show the list of avaliable products\n\n -add : put some good on sale\n\n -info: show information about your profile(username, bank account and purchases)\n\n -exit : close the application\n\n")

		case "-buy":
			var buyName string
			var prod model.Product
			fmt.Println("Enter the name of product you want to buy:")
			fmt.Scanln(&buyName)
			resp, err := http.Get(addr["prod_list"] + "/" + buyName)
			if err != nil {
				log.Fatal(err)
			}
			body, _ := ioutil.ReadAll(resp.Body)
			defer resp.Body.Close()

			err = json.Unmarshal(body, &prod)
			if err != nil {
				log.Fatal(err)
			}
			if prod.Name == "" {
				fmt.Println("Product not found.")

			} else if prod.Price <= user.Money {
				user.Money -= prod.Price
				user.Purchases = append(user.Purchases, prod.Name)
				_, err = http.Post(addr["user_list"]+
					"/"+user.Username+
					"/"+strconv.FormatFloat(user.Money, 'f', 6, 64)+
					"/"+prod.Name, "application/json", bytes.NewBuffer(userJson))
				if err != nil {
					fmt.Println(err)
					return
				}
				req, err := http.NewRequest("DELETE", addr["prod_list"]+"/"+prod.Name, nil)
				if err != nil {
					fmt.Println(err)
					return
				}
				_, err = client.Do(req)
				if err != nil {
					fmt.Println(err)
					return
				}
				fmt.Printf("%s purchased successful.\n Your balance: %f\n", prod.Name, user.Money)

			} else {
				fmt.Println("Oops! It seems that you do not have enough money!")
			}

		case "-show":
			resp, err := http.Get(addr["prod_list"])
			if err != nil {
				log.Fatal(err)
			}
			body, _ := ioutil.ReadAll(resp.Body)
			fmt.Println(string(body))
			defer resp.Body.Close()

		case "-add":
			var prod model.Product
			fmt.Println("Enter product's name and price:")
			fmt.Scanln(&prod.Name, &prod.Price)
			prodJson, err := json.Marshal(prod)
			if err != nil {
				fmt.Println("Error during marshaling the good. Try again.")
			} else {
				_, err = http.Post(addr["prod_list"], "application/json", bytes.NewBuffer(prodJson))
				if err != nil {
					fmt.Printf("Error during good appending to the server:%v. Try again.", err)
				}
			}

		case "-info":
			fmt.Printf("Username: %s\n\n", user.Username)
			fmt.Printf("Balance: %f USD\n\n", user.Money)
			fmt.Printf("Purchased: %s\n\n", user.Purchases)

		case "-exit":
			syscall.Exit(0)

		default:
			fmt.Println("Print -help for the list of actions.")
		}
	}
}
