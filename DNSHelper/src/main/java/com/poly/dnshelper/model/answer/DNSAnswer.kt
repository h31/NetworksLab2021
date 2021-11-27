package com.poly.dnshelper.model.answer

import com.poly.dnshelper.Util.getBytesFromInt
import com.poly.dnshelper.Util.getBytesFromShort
import com.poly.dnshelper.Util.getIntFromBytes
import com.poly.dnshelper.Util.getShortFromTwoBytes

abstract class DNSAnswer(
    var name: ByteArray = byteArrayOf(0xC0.toByte(), 0x0Cu.toByte()),
    var type: Short = 0,
    var dnsClass: Short = 0,
    var timeToLive: Int = 0,
    var dataLength: Short = 0,
    var resourceData: ByteArray = byteArrayOf()
) {

    open fun getAnswerBytes(): List<Byte> {
        val resultArrayBytes = mutableListOf<Byte>()
        resultArrayBytes.addAll(name.toList())
        resultArrayBytes.addAll(getBytesFromShort(type))
        resultArrayBytes.addAll(getBytesFromShort(dnsClass))
        resultArrayBytes.addAll(getBytesFromInt(timeToLive))
        resultArrayBytes.addAll(getBytesFromShort(dataLength))
        return resultArrayBytes
    }

    open fun mapperAnswer(byteArray: ByteArray) {
        val sizeName = 2
        type = getShortFromTwoBytes(byteArray[sizeName] to byteArray[sizeName + 1])
        dnsClass = getShortFromTwoBytes(byteArray[sizeName + 2] to byteArray[sizeName + 3])
        timeToLive = getIntFromBytes(
            byteArray
                .toList()
                .subList(sizeName + 4, sizeName + 8)
                .toByteArray()
        )
        dataLength = getShortFromTwoBytes(byteArray[sizeName + 8] to byteArray[sizeName + 9])
    }

    abstract fun getSize(byteArray: ByteArray): Int

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DNSAnswer

        if (type != other.type) return false
        if (dnsClass != other.dnsClass) return false
        if (timeToLive != other.timeToLive) return false
        if (dataLength != other.dataLength) return false
        if (!resourceData.contentEquals(other.resourceData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type
        result = 31 * result + dnsClass
        result = 31 * result + timeToLive
        result = 31 * result + dataLength
        result = 31 * result + resourceData.contentHashCode()
        return result
    }
}
