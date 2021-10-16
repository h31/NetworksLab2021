package protocol

import (
	"strconv"
	"time"
)

func GetCurTime() ([]byte, error) {
	t := time.Now().Format("1504")

	h, err := strconv.Atoi(t[:2])
	if err != nil {
		return nil, err
	}

	m, err := strconv.Atoi(t[2:])
	if err != nil {
		return nil, err
	}

	return []byte{byte(h), byte(m)}, nil
}

func IntToByteArr(size int) []byte {
	res := toBitArray(size)
	res = toByteArray(res)
	return res
}

func ByteArrToInt(arr []byte) int {
	res := 0
	arrLen := len(arr)

	for byteC := 0; byteC < arrLen; byteC++ {
		res += int(arr[byteC]) * powInt(2, byteC*8)
	}
	return res
}

// int number to array of bits
func toBitArray(n int) []byte {
	bitArrLen := bitArrLen(n)
	for bitArrLen%8 != 0 {
		bitArrLen++
	}

	bitArr := make([]byte, bitArrLen)

	for x := 0; x < len(bitArr); x++ {
		bitArr[x] = byte(n % 2)
		n /= 2
	}

	return bitArr
}

// array of bits to array of bytes (bit array group by 8 bits)
func toByteArray(bitArr []byte) []byte {
	byteArr := make([]byte, (len(bitArr) / 8))

	curNum := 0 // current byte in byte array
	count := 0  // offset
	byteC := 0  // byte index in byte array

	for bitC := 0; bitC < len(bitArr); bitC++ { // bitC - powInter
		if bitC%8 == 0 && bitC != 0 {
			byteArr[byteC] = byte(curNum)
			count += 8
			byteC++
			curNum = 0
		}

		curNum += int(bitArr[bitC]) * powInt(2, bitC-count)

		if bitC == (len(bitArr) - 1) {
			byteArr[byteC] = byte(curNum)
		}
	}
	return byteArr
}

// len of a int number in binary form
func bitArrLen(n int) int {
	if n == 0 {
		return 1
	}
	c := 0
	for n > 0 {
		n = n >> 1
		c++
	}
	return c
}

func powInt(n int, p int) int {
	res := 1
	for p > 0 {
		res *= n
		p--
	}
	return res
}
