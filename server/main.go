package main

import (
	"bufio"
	"errors"
	"fmt"
	"net"
	"github.com/petrain49/TCPchat/protocol"
)

func main() {

	fmt.Println("Start server...")

	newServer := startServer(10)

	i := 0
	ln, err := net.Listen("tcp", ":8081")
	if err != nil {
		fmt.Println("Error with listening")
	}

	for {
		conn, err := ln.Accept()
		if err != nil {
			continue
		}
		defer conn.Close()

		newServer.users[i] = protocol.User{
			Id:         i,
			Connection: conn,
		}

		go connection(*newServer, i)
		i++
	}
}

func startServer(n int) *protocol.Server {
	clients := make(map[int]protocol.User, n)
	return &protocol.Server{
		Users: clients,
	}
}

func connection(server protocol.Server, i int) error {
	client := server.Users[i]
	err := greeting(&client)
	if err != nil {
		return errors.New("Connection failed at greeting")
	}
	for {

		message, err := client.ReadBuf()
		if err != nil {
			return err
		}

		fmt.Print(string(message))

		broadcast(message)
	}
}

func greeting(client *protocol.User) error {
	
	err := client.SendCommand(CMD_NAME)
	if err != nil {
		return err
	}

	name, err := client.ReadBuf()
	if err != nil {
		return err
	}

	err = client.SendCommand(CMD_TIME)
	if err != nil {
		return err
	}

	timeZone, err := client.ReadBuf()
	if err != nil {
		return err
	}

	client.userName = name
	client.timeZone = timeZone
}

func broadcast(msg string) 


