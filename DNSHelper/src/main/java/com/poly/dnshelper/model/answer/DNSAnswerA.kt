package com.poly.dnshelper.model.answer

import com.poly.dnshelper.Util.getShortFromTwoBytes

class DNSAnswerA(
    name: ByteArray = byteArrayOf(0xC0.toByte(), 0x0Cu.toByte()),
    type: Short = 0,
    dnsClass: Short = 0,
    timeToLive: Int = 0,
    dataLength: Short = 0,
    resourceData: ByteArray = byteArrayOf()
) : DNSAnswer(name, type, dnsClass, timeToLive, dataLength, resourceData) {

    override fun getAnswerBytes(): List<Byte> {
        val newList = super.getAnswerBytes().toMutableList()
        newList.addAll(resourceData.toList())
        return newList
    }

    override fun mapperAnswer(byteArray: ByteArray) {
        super.mapperAnswer(byteArray)
        val sizeName = 2
        if (dataLength != (4).toShort()) throw IllegalArgumentException()
        resourceData = byteArray
            .toList()
            .subList(sizeName + 10, sizeName + 10 + dataLength)
            .toByteArray()
    }

    /**
    shift= name(2) + type(2) + class(2) + ttl(4)
    size = shift + dataLength(2) + valueOf(datalength)
     */
    override fun getSize(byteArray: ByteArray): Int {
        val shift = 2 + 2 + 2 + 4
        return shift + 2 + getShortFromTwoBytes(byteArray[shift] to byteArray[shift + 1])
    }
}