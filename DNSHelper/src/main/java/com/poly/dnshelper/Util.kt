package com.poly.dnshelper

internal object Util {
    fun getBytesFromShort(value: Short): List<Byte> {
        val rightByte: Byte = value.toByte()
        val leftByte: Byte = value.toInt().shr(8).toByte()
        return listOf(leftByte, rightByte)
    }

    fun getBytesFromInt(value: Int): List<Byte> {
        return listOf(
            value.shr(24).toByte(),
            value.shr(16).toByte(),
            value.shr(8).toByte(),
            value.toByte()
        )
    }

    fun parseToCorrectForm(byte: Byte): String {
        val binaryString = Integer.toBinaryString(byte.toInt())
        return if (binaryString.length > 16) {
            binaryString.substring(24)
        } else if (binaryString.length < 8) {
            val newString = StringBuilder()
            for (i in 0 until 8 - binaryString.length) {
                newString.append(0)
            }
            newString.append(binaryString).toString()
            newString.toString()
        } else {
            binaryString
        }
    }

    fun getShortFromTwoBytes(leftAndRightBytes: Pair<Byte, Byte>): Short {
        var result = leftAndRightBytes.first.toShort()
        result = result.toInt().shl(8).toShort()
        return (result + leftAndRightBytes.second).toShort()
    }

    fun getIntFromBytes(byteArray: ByteArray): Int {
        var result = 0
        for (i in 0 until 4) {
            result = result.shl(8)
            result += byteArray[i].toInt()
        }
        return result
    }
}
