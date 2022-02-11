package main

import (
	"bufio"
	"fmt"
	"io"
	"log"
	"net"
	"os"
	"os/signal"
	"strings"

	"github.com/gookit/color"
)

func main() {
	conn, err := net.Dial("tcp", "127.0.0.1:8081")
	if err != nil {
		log.Fatal(err)
	}
	defer conn.Close()

	reader := bufio.NewReader(os.Stdin)
	color.Cyan.Print("Enter your username: ")
	username, err := reader.ReadString('\n')
	if err != nil {
		log.Fatal(err)
	}
	username = strings.Trim(username, " \r\n")
	fmt.Fprintf(conn, "Welcome, %s!\n", username)
	color.Green.Printf("Welcome, %s!\n", username)

	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)
	go func() {
		log.Printf("\nCaptured %v, exiting..", <-c)
		fmt.Fprintf(conn, username+" is leaving the server..."+"\n")
		os.Exit(1)
	}()
	//reading msgs from server
	go messageReader(conn)
	//sending msgs to the server
	messageWriter(username, conn)

}

func messageWriter(username string, conn net.Conn) {
	for {
		reader := bufio.NewReader(os.Stdin)
		text, err := reader.ReadString('\n')

		if err == io.EOF {
			conn.Close()
			fmt.Println("\nConnection closed.")
			os.Exit(0)
		}
		fmt.Fprintf(conn, username+": "+strings.Trim(text, " \r\n")+"\n")
	}
}

func messageReader(conn net.Conn) {
	for {
		message, err := bufio.NewReader(conn).ReadString('\n')
		if err == io.EOF {
			conn.Close()
			fmt.Println("\nConnection closed.")
			os.Exit(0)
		}
		fmt.Println(message)

	}
}
