package com.poly.dnshelper.model.answer

import com.poly.dnshelper.Util

class DNSAnswerAAAA(
    name: ByteArray = byteArrayOf(0xC0.toByte(), 0x0C.toByte()),
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
        if (dataLength != (16).toShort()) throw IllegalArgumentException()
        resourceData = byteArray
            .toList()
            .subList(sizeName + 10, sizeName + 10 + dataLength)
            .toByteArray()
    }

    override fun getSize(byteArray: ByteArray): Int {
        val shift = 2 + 2 + 2 + 4
        return shift + 2 + Util.getShortFromTwoBytes(byteArray.slice(shift..shift + 1).toByteArray())
    }
}