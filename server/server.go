package main

/*
TODO: fix this
*/

import (
	"bufio"
	"errors"
	"net"
	"strconv"
	"time"
	"TCPchat/protocol"
)

type Server struct {
	Users map[int]User
}

type User struct {
	Id         int
	Connection net.Conn
	UserName   string
}

func (client User) SendMessage(message protocol.Message) error {
	if client.Connection == nil {
		return errors.New("There is no connection attached to the user")
	}

	_, err := client.Connection.Write(arr)
	if err != nil {
		return err
	}
	return nil
}

func (client User) ReadBuf() (string, error) {
	if client.Connection == nil {
		return "", errors.New("There is no connection attached to the user")
	}

	res, err := bufio.NewReader(client.Connection).ReadString('\n')
	if err != nil {
		return "", err
	}

	return res, nil
}

func (server Server) Broadcast(message *Message) {
	for _, client := range server.Users {
		client.Connection.Write([]byte(message + "\n"))
	}
}


