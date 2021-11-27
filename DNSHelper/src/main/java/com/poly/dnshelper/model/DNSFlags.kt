package com.poly.dnshelper.model

import com.poly.dnshelper.Util.parseToCorrectForm

data class DNSFlags(
    var isResponse: Boolean = false,
    var opCode: Byte = 0,
    var aa: Boolean = false,
    var truncated: Boolean = false,
    var recursionDesired: Boolean = false,
    var recursionAccepted: Boolean = false,
    var rCode: Byte = 0
) {

    private var bytes: Int = 0

    fun getBytes(): Short {
        bitOperation(isResponse, 4)
        bitOperation(opCode.toInt(), 1)
        bitOperation(aa, 1)
        bitOperation(truncated, 1)
        bitOperation(recursionDesired, 1)
        bitOperation(recursionAccepted, 4)
        bitOperation(0, 3)
        bitOperation(rCode.toInt())
        return bytes.toShort()
    }

    fun mapperFlags(leftAndRightByte: Pair<Byte, Byte>) {
        setLeftByte(leftAndRightByte.first)
        setRightByte(leftAndRightByte.second)
    }


    private fun bitOperation(value: Boolean, shift: Int) {
        if (value) bytes = bytes.or(1)
        bytes = bytes.shl(shift)
    }

    private fun bitOperation(value: Int, shift: Int) {
        bytes = bytes.or(value)
        bytes = bytes.shl(shift)
    }

    private fun bitOperation(value: Int) {
        bytes = bytes.or(value)
    }

    private fun setLeftByte(byte: Byte) {
        val binaryString = parseToCorrectForm(byte)
        isResponse = isPositive(binaryString[0])
        opCode = Integer.parseInt(binaryString.substring(1, 5), 2).toByte()
        aa = isPositive(binaryString[5])
        truncated = isPositive(binaryString[6])
        recursionDesired = isPositive(binaryString[7])
    }

    private fun setRightByte(byte: Byte) {
        val binaryString = parseToCorrectForm(byte)
        recursionAccepted = isPositive(binaryString[0])
        rCode = Integer.parseInt(binaryString.substring(4, 8), 2).toByte()
    }

    private fun isPositive(char: Char) = char == '1'
}
