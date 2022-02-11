package main

import (
	"bufio"
	"fmt"
	"io"
	"log"
	"net"
	"time"
)

var (
	openConnections = make(map[net.Conn]bool)
	newConnection   = make(chan net.Conn)
	deadConnection  = make(chan net.Conn)
)

func main() {
	fmt.Println("Server launch...")
	listener, err := net.Listen("tcp", ":8081")
	if err != nil {
		log.Fatal("Error through server initialization.", err)
	}
	defer listener.Close()

	fmt.Printf("Server launched at port %s.", listener.Addr())

	go func() {
		for {
			conn, err := listener.Accept()
			if err != nil {
				log.Fatal(err)
			}
			openConnections[conn] = true
			//making newConnection chan to use connection outside the goroutine
			newConnection <- conn
			fmt.Printf("New client connected on port %s \n", listener.Addr().String())
		}

	}()

	for {
		select {
		case conn := <-newConnection:
			//implement msg broadcast to another connections
			go broadcastMsg(conn)
		case conn := <-deadConnection:
			for elt := range openConnections {
				if elt == conn {
					fmt.Printf("Client disconnected.\n")
					break
				}
			}
			delete(openConnections, conn)
		}
	}

}

func broadcastMsg(conn net.Conn) {
	for {
		reader := bufio.NewReader(conn)
		msg, err := reader.ReadString('\n')
		if err == io.EOF {
			break
		}
		timeMsg := time.Now().Format("2006-01-02 15:04:05") + " " + msg
		fmt.Println(timeMsg)
		for elt := range openConnections {
			if elt != conn {
				elt.Write([]byte(timeMsg))
			}
		}
	}
	deadConnection <- conn
}
