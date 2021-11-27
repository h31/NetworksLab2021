package com.poly.dnshelper.model.answer

import com.poly.dnshelper.Util.getBytesFromShort
import com.poly.dnshelper.Util.getShortFromTwoBytes

class DNSAnswerMX(
    var preference: Short = 0,
    name: ByteArray = byteArrayOf(0xC0.toByte(), 0x0Cu.toByte()),
    type: Short = 0,
    dnsClass: Short = 0,
    timeToLive: Int = 0,
    dataLength: Short = 0,
    resourceData: ByteArray = byteArrayOf()
) : DNSAnswer(name, type, dnsClass, timeToLive, dataLength, resourceData) {

    override fun getAnswerBytes(): List<Byte> {
        val newList = super.getAnswerBytes().toMutableList()
        newList.addAll(getBytesFromShort(preference))
        newList.addAll(resourceData.toList())
        return newList
    }

    override fun mapperAnswer(byteArray: ByteArray) {
        super.mapperAnswer(byteArray)
        val sizeName = 2
        preference = getShortFromTwoBytes(byteArray[sizeName + 10] to byteArray[sizeName + 11])
        resourceData = byteArray
            .toList()
            .subList(sizeName + 12, sizeName + 12 + dataLength - 2)
            .toByteArray()
    }

    override fun getSize(byteArray: ByteArray): Int {
        val shift = 2 + 2 + 2 + 4
        return shift + 2 + getShortFromTwoBytes(byteArray[shift] to byteArray[shift + 1])
    }
}