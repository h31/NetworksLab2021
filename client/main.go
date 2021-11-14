package main

import (
	"bufio"
	"fmt"
	"net"
	"os"
)

// os.ReadFile(string)

func main() {
	conn, err := net.Dial("tcp", "127.0.0.1:8081")
	if err != nil {
        fmt.Println("dial error:", err)
        return
    }
	defer conn.Close()

	buf := make([]byte, 0, 4096)
	tmp := make([]byte, 256)

	for {
		n, err := conn.Read(tmp)
        if err != nil {
            if err != io.EOF {
                fmt.Println("read error:", err)
            }
            break
        }
        buf = append(buf, tmp[:n]...)
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
