package main

import (
	"errors"
	"math/rand"
)

const (
	QTYPE_A    = 1
	QTYPE_AAAA = 28
	QTYPE_TXT  = 16
	QTYPE_MX   = 15
)

var mailServerName = []string{"my", "mail", "com"}

var ErrUnsupportedQType = errors.New("unsupported qtype")

func randBytes(n int) (b []byte) {
	b = make([]byte, n)
	for i := 0; i < n; i++ {
		b[i] = byte(rand.Int())
	}
	return
}

func makeTxtRData(str string) []byte {
	b := []byte{byte(len(str))}
	b = append(b, []byte(str)...)
	return b
}

//returns (RR, nil) if resource exists or (nil, err) if not
func GetRR(q *DnsQuestion) (*DnsResourceRecord, error) {
	rr := new(DnsResourceRecord)

	rr.Name = q.QName
	rr.Type = q.QType
	rr.Class = q.QClass
	rr.TTL = 32

	switch q.QType {
	case QTYPE_A:
		b := randBytes(4)
		rr.RDLength = uint16(len(b))
		rr.RData = b
	case QTYPE_AAAA:
		b := randBytes(16)
		rr.RDLength = uint16(len(b))
		rr.RData = b
	case QTYPE_TXT:
		b := makeTxtRData("Sample string")
		rr.RDLength = uint16(len(b))
		rr.RData = b
	case QTYPE_MX:
		b := []byte{0, 1} //preference
		b = append(b, packName(mailServerName)...)
		rr.RDLength = uint16(len(b))
		rr.RData = b
	default:
		return nil, ErrUnsupportedQType
	}

	return rr, nil
}
