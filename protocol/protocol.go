package protocol

import (
	"errors"
)

const (
	CMD_NONE byte = iota
	CMD_NAME
)

type message struct {
	fields     byte
	lengths    []byte
	command    []byte
	username   []byte
	serverTime []byte
	text       []byte
	file       []byte
}

func NewMessage() *message {
	return new(message)
}

func (m *message) SetCommand(cmd byte) error {
	byteCmd, err := MakeField(cmd)
	if err != nil {
		return err
	}

	m.command = byteCmd
}

func (m *message) PackMessage() ([]byte, error) {
	return nil, nil
}


func MakeField(i interface{}) ([]byte, error) {
	data := []byte{}

	switch i.(type) {
	case string:
		data = []byte(i.(string))
	case int:
		data = IntToByteArr(i.(int))
	case byte:
		data = []byte{i.(byte)}
	default:
		return nil, errors.New("Wrong type")
	}

	dataLength := len(data)
	dataByteLength := IntToByteArr(dataLength)

	field := make([]byte, 0, dataLength+len(dataByteLength))
	field = append(append(field, dataByteLength...), data...)

	return field, nil
}
