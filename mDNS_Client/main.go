package main

import (
	"fmt"
	"math/rand"
	"net"
	"os"
	"strings"
	"time"
)

var typeStrings = map[uint16]string{
	QTYPE_A:    "A",
	QTYPE_AAAA: "AAAA",
	QTYPE_MX:   "MX",
	QTYPE_TXT:  "TXT",
}

const (
	DnsServerIP = "8.8.8.8:53"
)

func parsePtrRecursively(b []byte, ptr int) []string {
	wLen, name, endsWithPtr := unpackName(b, ptr)
	if !endsWithPtr {
		return name
	}
	ptr += wLen
	ptr = convertPtr(b[ptr], b[ptr+1])
	return append(name, parsePtrRecursively(b, ptr)...)
}

func convertPtr(a, b byte) int {
	return (int(a&0x3f) << 8) | int(b)
}

func main() {

	question := DnsQuestion{}

	if len(os.Args) != 3 {
		os.Exit(-1)
	}

	switch os.Args[1] {
	case "TXT":
		question.QType = QTYPE_TXT
	case "A":
		question.QType = QTYPE_A
	case "AAAA":
		question.QType = QTYPE_AAAA
	case "MX":
		question.QType = QTYPE_MX
	default:
		fmt.Println("unknown type")
		os.Exit(-1)
	}

	question.QName = strings.Split(os.Args[2], ".")
	// question.QType = QTYPE_MX
	// question.QName = []string{"gmail", "com"}
	question.QClass = CLASS_IN

	dnsReq := DnsRequest{
		Id:        uint16(rand.Uint32()),
		QR:        false,
		Opcode:    0,
		TC:        false,
		RD:        true,
		Z:         0,
		QDcount:   1,
		Questions: []*DnsQuestion{&question},
	}

	udpSock, _ := net.Dial("udp", "8.8.8.8:53")
	// udpSock, _ := net.Dial("udp", "192.168.1.45:6666")

	b, _ := dnsReq.MarshalBinary()
	udpSock.Write(b)

	buf := make([]byte, 10000)
	tim := time.NewTimer(time.Second * 5)
	for alive := true; alive; {
		select {
		case <-tim.C:
			fmt.Println("No response")
			alive = false
		default:
			l, _ := udpSock.Read(buf)
			if l != 0 {
				resp := DnsRequest{}
				resp.UnmarshalBinary(buf[:l])

				if resp.TC { //should use TCP
					sock, _ := net.Dial("tcp", DnsServerIP)
					reqLen := len(b)
					sock.Write([]byte{byte(reqLen >> 8), byte(reqLen)})
					sock.Write(b)
					sock.Write([]byte{0, 0})

					//TODO data may be received via multiple Read() calls
					tcpBuf := make([]byte, 0, 10000)
					tcpReadLen, _ := sock.Read(buf)
					pos := 0
					for tcpReadLen != 0 {
						fLen := int(buf[0])<<8 | int(buf[1])
						pos += 2
						tcpReadLen -= 2
						tcpBuf = append(tcpBuf, buf[pos:pos+fLen]...)
						tcpReadLen -= fLen
						pos += fLen
					}
					resp.UnmarshalBinary(tcpBuf)
					buf = tcpBuf
				}

				for i, rr := range resp.ResourceRecords {
					fmt.Printf("Answer #%d:\n", i)
					fmt.Printf("\tname = %s\n", strings.Join(rr.Name, "."))
					fmt.Printf("\tTTL = %d\n", rr.TTL)
					fmt.Printf("\ttype = %s\n", typeStrings[rr.Type])
					switch rr.Type {
					case QTYPE_A, QTYPE_AAAA:
						fmt.Printf("\taddr = %s\n", net.IP(rr.RData).String())
					case QTYPE_TXT:
						fmt.Printf("\ttext = %s\n", string(rr.RData[1:rr.RData[0]+1]))
					case QTYPE_MX:
						fmt.Printf("\tpreference = %d\n", (int(rr.RData[0])<<8)|int(rr.RData[1]))
						wLen, exchangerName, endsWithPtr := unpackName(rr.RData, 2)
						rDataPos := 2 + wLen
						if endsWithPtr {
							offset := convertPtr(rr.RData[rDataPos], rr.RData[rDataPos+1])
							exchangerName = append(exchangerName, parsePtrRecursively(buf, offset)...)
						}
						fmt.Printf("\tmail exchanger = %s\n", strings.Join(exchangerName, "."))
					}
				}
			}
			alive = false
		}
	}

}
