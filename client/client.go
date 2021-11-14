package main

import (
	"TCPchat/protocol"
	"bytes"
	"errors"
	"io"
	"net"
)

type messageFromServer struct {
	text          []byte
	file          []byte
	username      []byte
	serverTime    []byte
}

func readBytes(conn net.Conn) ([]byte, error) {
	var buf bytes.Buffer
	io.Copy(&buf, conn)

	arr := make([]byte, 0, buf.Len())
	_, err := buf.Read(arr)
	if err != nil {
		return nil, errors.New("cant read the buffer")
	}

	return arr, nil
}

func readMessageInfo(arr []byte) (int, []int) {
	fields := int(arr[0])
	lengths := make([]int, fields)
	for x := 1; x <= fields; x++ {
		lengths[x-1] = int(arr[x])
	}
	return fields, lengths
}

func readMessage(arr []byte) (*messageFromServer, error) {
	msg := new(messageFromServer)

	fields, lengths := readMessageInfo(arr)

	content := arr[fields + 1:]
	var shift int
	var curLen int
	var lenEnd int
	var fieldEnd int

	for x, b := range lengths {
		lenEnd = shift + b
		curLen = protocol.ByteArrToInt(content[shift:lenEnd])
		fieldEnd = lenEnd + curLen

		switch x {
		case 0:
			msg.text = content[lenEnd:fieldEnd]
		case 1:
			msg.file = content[lenEnd:fieldEnd]
		case 2:
			msg.username = content[lenEnd:fieldEnd]
		case 3:
			msg.serverTime = content[lenEnd:fieldEnd]
		default:
			return msg, nil
		}
	}
	return msg, nil
}
