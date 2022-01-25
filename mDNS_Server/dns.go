package main

type DnsQuestion struct {
	QName  []string
	QType  uint16
	QClass uint16
}

type DnsResourceRecord struct {
	Name     []string
	Type     uint16
	Class    uint16
	TTL      uint32
	RDLength uint16
	RData    []byte
}

type DnsRequest struct {
	Id uint16

	QR     bool
	Opcode byte
	AA     bool
	TC     bool
	RD     bool

	RA    bool
	Z     byte
	RCode byte

	QDcount uint16
	ANcount uint16
	NScount uint16
	ARcount uint16

	Questions       []*DnsQuestion
	ResourceRecords []*DnsResourceRecord
}

func packName(name []string) []byte {
	b := []byte{}
	for _, str := range name {
		b = append(b, byte(len(str)))
		b = append(b, []byte(str)...)
	}
	b = append(b, 0)
	return b
}

//return length in bytes
func unpackName(b []byte, start int) (int, []string) {
	strArr := []string{}
	i := start
	for b[i] != 0 {
		strArr = append(strArr, string(b[i+1:int(b[i])+i+1]))
		i += int(b[i]) + 1
	}
	return i - start, strArr
}

func (req *DnsRequest) UnmarshalBinary(b []byte) error {
	req.Questions = make([]*DnsQuestion, 0)
	req.ResourceRecords = make([]*DnsResourceRecord, 0)

	req.Id = uint16(b[0]) << 8
	req.Id |= uint16(b[1])
	req.QR = (uint(b[2])>>7)&1 == 1
	req.Opcode = (b[3] >> 3) & 0xF
	req.AA = (uint(b[2])>>2)&1 == 1
	req.TC = (uint(b[2])>>1)&1 == 1
	req.RD = uint(b[2])&1 == 1

	req.RA = (uint(b[3])>>7)&1 == 1
	req.Z = (b[3] >> 4) & 0x7
	req.RCode = b[3] & 0xf

	req.QDcount = uint16(b[4]) << 8
	req.QDcount |= uint16(b[5])
	req.ANcount = uint16(b[6]) << 8
	req.ANcount |= uint16(b[7])
	req.NScount = uint16(b[8]) << 8
	req.NScount |= uint16(b[9])
	req.ARcount = uint16(b[10]) << 8
	req.ARcount |= uint16(b[11])

	pos := int(12)
	for i := 0; i < int(req.QDcount); i++ { // loop over questions
		question := new(DnsQuestion)
		//parse qname
		for b[pos] != 0 {
			question.QName = append(question.QName, string(b[pos+1:int(b[pos])+pos+1]))
			pos += int(b[pos]) + 1
		}
		pos += 1
		question.QType = uint16(b[pos]) << 8
		pos += 1
		question.QType |= uint16(b[pos])
		pos += 1
		question.QClass = uint16(b[pos]) << 8
		pos += 1
		question.QClass |= uint16(b[pos])
		pos += 1
		req.Questions = append(req.Questions, question)
	}

	for i := 0; i < int(req.ANcount); i++ { // loop over questions
		rr := new(DnsResourceRecord)
		for b[pos] != 0 {
			//parse name
			if b[pos]&0xc0 != 0 { //a pointer
				offset := (int(b[pos]&0x3f) << 8) | int(b[pos+1])
				_, name := unpackName(b, offset)
				pos += 2
				rr.Name = append(rr.Name, name...)
				break
			} else {
				l, name := unpackName(b, pos)
				pos += l
				rr.Name = append(rr.Name, name...)
			}
		}
		rr.Type = uint16(b[pos]) << 8
		pos += 1
		rr.Type = uint16(b[pos])
		pos += 1
		rr.Class = uint16(b[pos]) << 8
		pos += 1
		rr.Class = uint16(b[pos])
		pos += 1
		rr.TTL = uint32(b[pos]) << 24
		pos += 1
		rr.TTL = uint32(b[pos]) << 16
		pos += 1
		rr.TTL = uint32(b[pos]) << 8
		pos += 1
		rr.TTL = uint32(b[pos])
		pos += 1
		rr.RDLength = uint16(b[pos]) << 8
		pos += 1
		rr.RDLength = uint16(b[pos])
		pos += 1

		rr.RData = make([]byte, rr.RDLength)
		copy(rr.RData, b[pos:pos+int(rr.RDLength)])
		req.ResourceRecords = append(req.ResourceRecords, rr)
	}

	return nil
}

func (r DnsRequest) MarshalBinary() ([]byte, error) {
	b := make([]byte, 12)

	r.QDcount = uint16(len(r.Questions))
	r.ANcount = uint16(len(r.ResourceRecords))

	b[0] = byte(r.Id >> 8)
	b[1] = byte(r.Id)
	if r.QR {
		b[2] = 1 << 7
	}
	b[2] |= (r.Opcode & 0xf) << 3
	if r.AA {
		b[2] |= 1 << 2
	}
	if r.TC {
		b[2] |= 1 << 1
	}
	if r.RD {
		b[2] |= 1
	}
	if r.RA {
		b[3] = 1 << 7
	}
	b[3] |= (((r.Z & 0x7) << 4) | (r.RCode & 0xf))

	b[4] = byte(r.QDcount >> 8)
	b[5] = byte(r.QDcount)
	b[6] = byte(r.ANcount >> 8)
	b[7] = byte(r.ANcount)
	b[8] = byte(r.NScount >> 8)
	b[9] = byte(r.NScount)
	b[10] = byte(r.ARcount >> 8)
	b[11] = byte(r.ARcount)
	nameOffsets := make([]int, 0)
	for _, q := range r.Questions {
		nameOffsets = append(nameOffsets, len(b))
		b = append(b, packName(q.QName)...)
		b = append(b,
			byte(q.QType>>8), byte(q.QType),
			byte(q.QClass>>8), byte(q.QClass),
		)
	}

	for i, rr := range r.ResourceRecords {
		// for _, str := range rr.Name {
		// 	b = append(b, []byte(str)...)
		// 	b = append(b, 0)
		// }
		// b = append(b, 0)

		b = append(b,
			byte(0xc0|(nameOffsets[i]>>8)), byte(nameOffsets[i]),
			byte(rr.Type>>8), byte(rr.Type),
			byte(rr.Class>>8), byte(rr.Class),
			byte(rr.TTL>>24), byte(rr.TTL>>16),
			byte(rr.TTL>>8), byte(rr.TTL),
			byte(rr.RDLength>>8), byte(rr.RDLength),
		)
		b = append(b, rr.RData...)
	}

	return b, nil
}
