/*	Package protocol declares format of Messages and implements common functions for both server and client.
		[Fields] - length of "Lengths" field - F,
		[Lengths] - how many bytes does the length of each field takes - L,
		[command] - mandatory field, single byte, requires hidden response,
		[Text] - mandatory field with Text from user,
		[File] - optional field with File from user,
		[Username] - additional field with sender Username added by the server,
		[ServerTime] - additional field with time of the Message added by the server.

	Example of a command message:
		[0] [COMMAND]
	Example of a text-only message:
		[1] [L(1)][Text] - from client to server
		[3] [len(L(1)), len(L(2)), len(L(3))] [L(1)][Text] [L(2)][Username] [L(3)][ServerTime] - from server to all clients
*/

package protocol

import (
	"errors"
)

const (
	CMD_QUIT byte = 1 + iota
	CMD_NAME
	CMD_ERROR
)

type Message struct {
	Fields     byte
	Lengths    []byte
	Text       []byte
	File       []byte
	Username   []byte
	ServerTime []byte
}

type CommandMessage struct {
	Fields  byte
	command byte
}

func NewMessage() Message {
	return Message{
		Lengths: make([]byte, 0, 5),
	}
}

func NewCommandMessage(cmd byte) CommandMessage {
	return CommandMessage{
		Fields:  0,
		command: cmd,
	}
}

func (m *Message) SetUsername(Username string) error {
	UsernameField, length, err := MakeField([]byte(Username))
	if err != nil {
		return err
	}

	m.Username = UsernameField
	m.Lengths = append(m.Lengths, length)
	m.Fields++

	return nil
}

func (m *Message) SetServerTime(ServerTime []byte) error {
	ServerTimeField, length, err := MakeField(ServerTime)
	if err != nil {
		return err
	}

	m.ServerTime = ServerTimeField
	m.Lengths = append(m.Lengths, length)
	m.Fields++

	return nil
}

func (m *Message) SetText(Text *string) error {
	TextField, length, err := MakeField(Text)
	if err != nil {
		return err
	}

	m.Text = TextField
	m.Lengths = append(m.Lengths, length)
	m.Fields++

	return nil
}

func (m *Message) SetFile(File []byte) error {
	FileField, length, err := MakeField(File)
	if err != nil {
		return err
	}

	m.File = FileField
	m.Lengths = append(m.Lengths, length)
	m.Fields++

	return nil
}

func (m *Message) PackMessage() ([]byte, error) {
	msgLen := len(m.Lengths) + len(m.Username) + len(m.ServerTime) + len(m.Text) + len(m.File) + 1
	msg := make([]byte, 0, msgLen)

	msg = append(msg, m.Fields)
	msg = append(msg, m.Lengths...)
	msg = append(msg, m.Text...)
	msg = append(msg, m.File...)
	msg = append(msg, m.Username...)
	msg = append(msg, m.ServerTime...)

	return msg, nil
}

// MakeField makes a field [len of data][data],
// returns field, number of bytes that form the length of data and error
func MakeField(i interface{}) ([]byte, byte, error) {
	var data []byte
	var err error

	switch t := i.(type) {
	case string:
		data = []byte(t)
	case int:
		data, err = IntToByteArr(t)
		if err != nil {
			return nil, 0, err
		}
	case byte:
		data = []byte{t}
	case []byte:
		data = t
	default:
		return nil, 0, errors.New("wrong type")
	}

	dataLength := len(data)
	dataLengthInBytes, err := IntToByteArr(dataLength)
	if err != nil {
		return nil, 0, err
	}

	field := make([]byte, 0, dataLength+len(dataLengthInBytes))
	field = append(append(field, dataLengthInBytes...), data...)

	return field, byte(len(dataLengthInBytes)), nil
}
