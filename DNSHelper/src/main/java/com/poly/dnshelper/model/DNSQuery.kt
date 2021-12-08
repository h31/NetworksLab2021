package com.poly.dnshelper.model

import com.poly.dnshelper.Util.getBytesFromShort
import com.poly.dnshelper.Util.getShortFromTwoBytes

data class DNSQuery(
    var name: String = "",
    var type: Short = 0,
    var queryClass: Short = 0
) {

    fun getQueryBytes(): List<Byte> {
        val resultArrayBytes = mutableListOf<Byte>()
        resultArrayBytes.addAll(bytesFromName(name))
        resultArrayBytes.addAll(getBytesFromShort(type))
        resultArrayBytes.addAll(getBytesFromShort(queryClass))
        return resultArrayBytes
    }

    fun mapperQuery(byteArray: ByteArray) {
        type = getShortFromTwoBytes(byteArray.slice(byteArray.size - 4..byteArray.size - 3).toByteArray())
        queryClass = getShortFromTwoBytes(byteArray.slice(byteArray.size - 2..byteArray.size - 1).toByteArray())
        name = nameFromBytes(byteArray.toList().subList(0, byteArray.size - 4).toByteArray())
    }

    private fun bytesFromName(name: String): List<Byte> {
        val parts = name.split(".")
        val bytes = mutableListOf<Byte>()
        parts.forEach {
            bytes.add(it.length.toByte())
            bytes.addAll(it.toByteArray().toList())
        }
        bytes.add(0)
        return bytes
    }

    private fun nameFromBytes(byteArray: ByteArray): String {
        val name = StringBuilder()
        var currentPos = 0
        var dot: Byte
        while (byteArray[currentPos] != (0).toByte() && byteArray.size - 1 != currentPos) {
            dot = byteArray[currentPos]
            if (byteArray.size - 1 != currentPos) {
                val currentWord = mutableListOf<Byte>()
                for (i in currentPos + 1..currentPos + dot) {
                    currentWord.add(byteArray[i])
                }
                name.append(String(currentWord.toByteArray()))
                name.append(".")
                currentPos += dot + 1
            }
        }
        val nameStr = name.toString()
        return nameStr.substring(0, nameStr.length - 1)
    }
}
