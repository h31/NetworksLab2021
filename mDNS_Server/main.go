package main

import (
	"fmt"
	"net"
	"os"
	"strings"
)

func main() {
	addr := net.UDPAddr{
		Port: 7777,
	}

	var req DnsRequest

	udpConn, err := net.ListenUDP("udp", &addr)

	if err != nil {
		os.Exit(-1)
	}
	buf := make([]byte, 1000)
	for {
		if bCount, addr, _ := udpConn.ReadFromUDP(buf); bCount != 0 {
			req.UnmarshalBinary(buf[:bCount])

			fmt.Println("Request recieved:")
			fmt.Printf("Question count = %d\n", len(req.Questions))
			for i, question := range req.Questions {
				fmt.Printf("Question #%d\n", i)
				fmt.Printf("\tType %d\n", question.QType)
				fmt.Printf("\tName %s\n", (strings.Join(question.QName, ".")))

				rr, err := GetRR(question)

				if err == nil {
					req.ANcount += 1
					req.ResourceRecords = append(req.ResourceRecords, rr)
				}
			}

			req.QR = true //set response
			resp, _ := req.MarshalBinary()
			udpConn.WriteToUDP(resp, addr)
		}
	}
}
