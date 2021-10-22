package protocol

import (
	"errors"
)

const (
	CMD_QUIT byte = 1 << iota
	CMD_NAME
	CMD_ERROR
)

type message struct {
	fields     byte // length of lengths field
	lengths    []byte // how many bytes does the length of each field takes
	command    []byte 
	username   []byte
	serverTime []byte
	text       []byte
	file       []byte
}

func NewMessage() message {
	return message{
		lengths: make([]byte, 0, 5),
	}
}

func (m *message) SetCommand(cmd byte) error {
	byteCmd, length, err := MakeField(cmd)
	if err != nil {
		return err
	}

	m.command = byteCmd
	m.lengths = append(m.lengths, length)
	m.fields++

	return nil
}

func (m *message) SetUsername(username string) error {
	usernameField, length, err := MakeField([]byte(username))
	if err != nil {
		return err
	}

	m.username = usernameField
	m.lengths = append(m.lengths, length)
	m.fields++

	return nil
}

func (m *message) SetServerTime(serverTime []byte) error {
	serverTimeField, length, err := MakeField(serverTime)
	if err != nil {
		return err
	}

	m.serverTime = serverTimeField
	m.lengths = append(m.lengths, length)
	m.fields++

	return nil
}

func (m *message) SetText(text *string) error {
	textField, length, err := MakeField(text)
	if err != nil {
		return err
	}

	m.text = textField
	m.lengths = append(m.lengths, length)
	m.fields++

	return nil
}

func (m *message) SetFile(file []byte) error {
	fileField, length, err := MakeField(file)
	if err != nil {
		return err
	}

	m.file = fileField
	m.lengths = append(m.lengths, length)
	m.fields++

	return nil
}

func (m *message) PackMessage() ([]byte, error) {
	msgLen := len(m.lengths) + len(m.command) + len(m.username) + len(m.serverTime) + len(m.text) + len(m.file) + 1
	msg := make([]byte, 0, msgLen)

	msg = append(msg, m.fields)
	msg = append(msg, m.lengths...)
	msg = append(msg, m.command...)
	msg = append(msg, m.username...)
	msg = append(msg, m.serverTime...)
	msg = append(msg, m.text...)
	msg = append(msg, m.file...)

	return msg, nil
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
	case []byte:
		data = t
	default:
		return nil, 0, errors.New("wrong type")
	}

	dataLength := len(data)
	dataByteLength := IntToByteArr(dataLength)

	field := make([]byte, 0, dataLength+len(dataByteLength))
	field = append(append(field, dataByteLength...), data...)

	return field, byte(len(dataByteLength)), nil
}
