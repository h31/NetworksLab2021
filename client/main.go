package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
)

// os.ReadFile(string)

func main() {

	// Подключаемся к сокету
	conn, _ := net.Dial("tcp", "127.0.0.1:8081")
	go reading(conn)
	for {
		// Чтение входных данных от stdin
		reader := bufio.NewReader(os.Stdin)
		fmt.Print("Text to send: ")
		text, _ := reader.ReadString('\n')
		// Отправляем в socket
		fmt.Fprintf(conn, text+"\n")
		// Прослушиваем ответ
		message, _ := bufio.NewReader(conn).ReadString('\n')
		fmt.Print("Message from server: " + message)
	}
}

func reading(conn net.Conn) {
	for {
		message, _ := bufio.NewReader(conn).ReadString('\n')
		fmt.Print("Message from server: " + message + "\n")
	}
}

//Format HH:MM:SS
//fmt.Printf("HH:MM:SS: %s\n", now.Format("03:04:05"))
// now.Zone()

/*
type greetings struct {
	username string
	timeZone string
}

type message struct {
	text string
}
*/
