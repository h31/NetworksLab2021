package com.monkeys.ntp.models

import com.monkeys.ntp.*

const val OFFSET_1900_TO_1970 = (365L * 70L + 17L) * 24L * 60L * 60L

class Header(
    var leapIndicator: Int = 0,                 //2 bits
    var versionNumber: Int = 4,                 //3 bits
    var mode: Int = 3,                          //3 bits      3 - client, 4 - server
    var stratum: Byte = 0,                      //8 bits
    var pool: Byte = 0,                         //8 bits
    var precision: Byte = 0,                    //8 bits
    var rootDelay: Long = 0,                     //32 bits
    var rootDispersion: Long = 0,                //32 bits
    var refId: Long = 0,                         //32 bits
    var reference: Long = 0,                    //64 bits
    var originate: Long = 0,                    //64 bits
    var receive: Long = 0,                      //64 bits
    var transmit: Long = 0                      //64 bits
){
    constructor(versionNumber: Int, mode: Int, transmit: Long) : this() {
        this.versionNumber = versionNumber
        this.mode = mode
        this.transmit = transmit
    }

    constructor(mode: Int, transmit: Long) : this() {
        this.mode = mode
        this.transmit = transmit
    }

    constructor(transmit: Long) : this() {
        this.transmit = transmit
    }

    constructor(array: ByteArray): this() {
        leapIndicator = array[0].toInt() shr 6 and 0x3
        versionNumber = array[0].toInt() shr 3 and 0x7
        mode = array[0].toInt() and 0x7
        stratum = array[1]
        pool = array[2]
        precision = array[3]
        rootDelay = convertTo32(array, ROOT_DELAY_OFFSET)
        rootDispersion = convertTo32(array, ROOT_DISPERSION_OFFSET)
        refId = convertTo32(array, REF_ID_OFFSET)
        reference = convertToTimestamp(array, REFERENCE_OFFSET)
        originate = convertToTimestamp(array, ORIGINATE_OFFSET)
        receive = convertToTimestamp(array, RECEIVE_OFFSET)
        transmit = convertToTimestamp(array, TRANSMIT_OFFSET)
    }

    private fun convertTo32(array: ByteArray, offset: Int): Long {
        val b0 = array[offset].toInt()
        val b1 = array[offset + 1].toInt()
        val b2 = array[offset + 2].toInt()
        val b3 = array[offset + 3].toInt()

        val i0 = if (b0 and 0x80 == 0x80) b0 and 0x7f or 0x80 else b0
        val i1 = if (b1 and 0x80 == 0x80) b1 and 0x7f or 0x80 else b1
        val i2 = if (b2 and 0x80 == 0x80) b2 and 0x7f or 0x80 else b2
        val i3 = if (b3 and 0x80 == 0x80) b3 and 0x7f or 0x80 else b3

        return (i0.toLong() shl 24) + (i1.toLong() shl 16) + (i2.toLong() shl 8) + i3.toLong()
    }

    fun convertToTimestamp(array: ByteArray, offset: Int): Long {
        val sec = convertTo32(array, offset)
        val frac = convertTo32(array, offset + 4)
        return ((sec - OFFSET_1900_TO_1970) * 1000) + ((frac * 1000L) / 0x100000000L);
    }

    fun toByteArray(): ByteArray {
        val ntpPacket = ByteArray(NTP_PACKET_SIZE)
        val referenceTime = ByteArray(8)
        val originateTime = ByteArray(8)
        val receiveTime = ByteArray(8)
        val transmitTime = ByteArray(8)
        val rootDelay = this.rootDelay.toByteArray()
        val rootDispersion = this.rootDispersion.toByteArray()
        val refId = this.refId.toByteArray()
        writeTimeStamp(referenceTime, this.reference)
        writeTimeStamp(originateTime, this.originate)
        writeTimeStamp(receiveTime, this.receive)
        writeTimeStamp(transmitTime, this.transmit)
        ntpPacket[0] = (NTP_VERSION.shl(3)).or(NTP_MODE_SERVER).toByte()
        ntpPacket[1] = this.stratum
        ntpPacket[2] = this.pool
        ntpPacket[3] = this.precision
        System.arraycopy(rootDelay, 0, ntpPacket, ROOT_DELAY_OFFSET, 4)
        System.arraycopy(rootDispersion, 0, ntpPacket, ROOT_DISPERSION_OFFSET, 4)
        System.arraycopy(refId, 0, ntpPacket, REF_ID_OFFSET, 4)
        System.arraycopy(referenceTime, 0, ntpPacket, REFERENCE_OFFSET, 8)
        System.arraycopy(transmitTime, 0, ntpPacket, TRANSMIT_OFFSET,8)
        System.arraycopy(originateTime, 0, ntpPacket, ORIGINATE_OFFSET, 8)
        System.arraycopy(receiveTime, 0, ntpPacket, RECEIVE_OFFSET, 8)
        return ntpPacket
    }

    fun printHeader() {
        println("Leap indicator: $leapIndicator\n" +
                "Version number: $versionNumber\n" +
                "Mode: $mode\n" +
                "Stratum: $stratum\n" +
                "Poll: $pool\n" +
                "Precision: $precision\n" +
                "Root delay: $rootDelay\n" +
                "Root dispersion: $rootDispersion\n" +
                "Ref id: $refId\n" +
                "Reference: $reference\n" +
                "Originate: $originate\n" +
                "Receive: $receive\n" +
                "Transmit: $transmit\n")
    }
}