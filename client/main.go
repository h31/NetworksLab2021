package main

import (
	"bufio"
	"fmt"
	"log"
	"net"
	"os"
	"os/signal"
	"strings"

	"github.com/gookit/color"
)

const bufsize = 128

func main() {
	conn, err := net.Dial("udp", "127.0.0.1:4444")
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

	go exitCapture(c, conn, username)
	//reading msgs from server
	go messageReader(conn)
	//sending msgs to the server
	messageWriter(username, conn)

}

func messageWriter(username string, conn net.Conn) {
	for {
		reader := bufio.NewReader(os.Stdin)
		text, err := reader.ReadString('\n')
		if err != nil {
			log.Fatal(err)
			break
		}
		fmt.Fprintf(conn, username+": "+strings.Trim(text, " \r\n")+"\n")
	}
}

func messageReader(conn net.Conn) {
	for {
		buf := make([]byte, bufsize)
		_, err := bufio.NewReader(conn).Read(buf)
		if err == nil {
			fmt.Printf("%s\n", buf)
		}
	}
}

func exitCapture(c chan os.Signal, conn net.Conn, username string) {
	log.Printf("\nCaptured %v, exiting..", <-c)
	fmt.Fprintf(conn, username+" is leaving the server..."+"\n")
	os.Exit(1)
}
