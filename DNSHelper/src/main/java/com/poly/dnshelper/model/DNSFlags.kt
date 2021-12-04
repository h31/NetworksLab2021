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
        addValueToBytes(isResponse, 4)
        addValueIntToBytes(opCode.toInt(), 1)
        addValueToBytes(aa, 1)
        addValueToBytes(truncated, 1)
        addValueToBytes(recursionDesired, 1)
        addValueToBytes(recursionAccepted, 4)
        addValueIntToBytes(0, 3)
        addIntValueToBytesDirectly(rCode.toInt())
        return bytes.toShort()
    }

    fun mapperFlags(leftAndRightByte: Pair<Byte, Byte>) {
        setLeftByte(leftAndRightByte.first)
        setRightByte(leftAndRightByte.second)
    }


    private fun addValueToBytes(value: Boolean, shift: Int) {
        if (value) bytes = bytes.or(1)
        bytes = bytes.shl(shift)
    }

    private fun addValueIntToBytes(value: Int, shift: Int) {
        bytes = bytes.or(value)
        bytes = bytes.shl(shift)
    }

    private fun addIntValueToBytesDirectly(value: Int) {
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
