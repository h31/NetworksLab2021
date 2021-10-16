package protocol

import (
	"bufio"
	"errors"
	"os"
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
	byteCmd, length, err := MakeField(cmd)
	if err != nil {
		return err
	}

	m.command = byteCmd
	m.lengths = append(m.lengths, length)

	return nil
}

func (m *message) SetUsername(username string) error {
	usernameField, length, err := MakeField([]byte(username))
	if err != nil {
		return err
	}

	m.username = usernameField
	m.lengths = append(m.lengths, length)

	return nil
}

func (m *message) SetServerTime(serverTime []byte) error {
	serverTimeField, length, err := MakeField(serverTime)
	if err != nil {
		return err
	}

	m.serverTime = serverTimeField
	m.lengths = append(m.lengths, length)

	return nil
}

func (m *message) SetText(text *string) error {
	textField, length, err := MakeField(text)
	if err != nil {
		return err
	}

	m.text = textField
	m.lengths = append(m.lengths, length)

	return nil
}

func (m *message) SetFile(file *os.File) error {
	fileField, length, err := MakeField(file)
	if err != nil {
		return err
	}

	m.file = fileField
	m.lengths = append(m.lengths, length)

	return nil
}

func (m *message) PackMessage() ([]byte, error) {
	return nil, nil
}


func MakeField(i interface{}) ([]byte, byte, error) {
	var data []byte

	switch t := i.(type) {
	case string:
		data = []byte(t)
	case int:
		data = IntToByteArr(t)
	case byte:
		data = []byte{t}
	case os.File:
		buffer := bufio.NewReader(&t)
		_, err := buffer.Read(data)
		if err != nil {
			return nil, 0, err
		}
	default:
		return nil, 0, errors.New("wrong type")
	}

	dataLength := len(data)
	dataByteLength := IntToByteArr(dataLength)

	field := make([]byte, 0, dataLength+len(dataByteLength))
	field = append(append(field, dataByteLength...), data...)

	return field, byte(len(dataByteLength)), nil
}
