package main

import (
	"fmt"
	"log"
	"net"
	"os"
	"os/signal"
	"time"
)

type Message struct {
	senderAddr *net.UDPAddr
	body       []byte
}

var (
	newMessage    = make(chan Message)
	checkPortList = []int{}
	addrList      = []*net.UDPAddr{}
)

func contains(s []int, e int) bool {
	for _, a := range s {
		if a == e {
			return true
		}
	}
	return false
}

func checkFatalError(err error) {
	if err != nil {
		fmt.Fprintf(os.Stderr, "Fatal error:%s", err.Error())
		os.Exit(1)
	}
}

func checkError(err error) {
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error:%s", err.Error())
	}
}

func main() {
	fmt.Println("Server launch...")
	addr := net.UDPAddr{
		IP:   net.ParseIP("127.0.0.1"),
		Port: 4444,
	}
	srv, err := net.ListenUDP("udp", &addr)
	checkFatalError(err)
	fmt.Printf("Server launched on port %s.\n", srv.LocalAddr().String())
	defer srv.Close()

	c := make(chan os.Signal, 1)
	signal.Notify(c, os.Interrupt)

	go exitCapture(c, srv)

	go broadcastMessage(srv)

	readMessage(srv)

}

func readMessage(srv *net.UDPConn) {
	for {
		buf := make([]byte, 128)
		_, clientUDPaddr, err := srv.ReadFromUDP(buf)
		checkError(err)
		if !contains(checkPortList, clientUDPaddr.Port) {
			checkPortList = append(checkPortList, clientUDPaddr.Port)
			addrList = append(addrList, clientUDPaddr)
		}
		fmt.Printf("New message on port %s \n", clientUDPaddr)
		newMessage <- Message{clientUDPaddr, buf}
	}
}

func broadcastMessage(s *net.UDPConn) error {
	for {
		msg := <-newMessage
		timeMsg := time.Now().Format("2006-01-02 15:04:05") + " " + string(msg.body)
		fmt.Println(timeMsg)
		for _, elt := range addrList {
			if elt.Port != msg.senderAddr.Port {
				_, err := s.WriteToUDP([]byte(timeMsg), elt)
				checkError(err)
			}
		}
	}
}

func exitCapture(c chan os.Signal, srv *net.UDPConn) {
	log.Printf("\nCaptured %v, exiting..", <-c)
	for _, elt := range addrList {
		_, err := srv.WriteToUDP([]byte("WARNING!Server stopped working."), elt)
		checkError(err)
	}
	os.Exit(2)
}
