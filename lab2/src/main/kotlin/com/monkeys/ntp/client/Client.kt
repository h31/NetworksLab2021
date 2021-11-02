package com.monkeys.ntp.client

import com.monkeys.ntp.*
import com.monkeys.ntp.models.Header
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class Client(val host: String, val port: Int) {

    fun start() {
        val socket = DatagramSocket()
        socket.use {
            println("Client started!!!")

            val ntpPacket = ByteArray(NTP_PACKET_SIZE)
            val clientTime = ByteArray(8)

            val packet = DatagramPacket(ntpPacket, ntpPacket.size, InetAddress.getByName("localhost"), 4445)
            val response = DatagramPacket(ntpPacket, ntpPacket.size)

            ntpPacket[0] = (NTP_VERSION.shl(3)).or(NTP_MODE_CLIENT).toByte()

            val requestTime = System.currentTimeMillis()
            writeTimeStamp(clientTime, requestTime)
            System.arraycopy(clientTime,0,ntpPacket, TRANSMIT_OFFSET, 8)

            //for read response
            val requestTicks: Long = System.nanoTime() / 1000000L

            socket.send(packet)
            val sendingHeader = Header(packet.data)
            sendingHeader.printHeader()

            socket.receive(response)

            val responseArray = response.data
            val h = Header(responseArray)
            checkValidServerResponse(h.leapIndicator, h.versionNumber, h.mode, h.stratum)
            h.printHeader()
        }

    }


//    private fun convertToLong(buffer: ByteArray, offset: Int): Long {
//        val b0 = buffer[offset].toInt()
//        val b1 = buffer[offset + 1].toInt()
//        val b2 = buffer[offset + 2].toInt()
//        val b3 = buffer[offset + 3].toInt()
//
//        // convert signed bytes to unsigned
//        val i0 = if ((b0 and 0x80) == 0x80) (b0 and 0x7F) + 0x80 else b0
//        val i1 = if (b1 and 0x80 == 0x80) (b1 and 0x7F) + 0x80 else b1
//        val i2 = if (b2 and 0x80 == 0x80) (b2 and 0x7F) + 0x80 else b2
//        val i3 = if (b3 and 0x80 == 0x80) (b3 and 0x7F) + 0x80 else b3
//        return (i0.toLong() shl 24) + (i1.toLong() shl 16) + (i2.toLong() shl 8) + i3.toLong()
//    }
//
//
//    fun readTimeStamp(buffer: ByteArray, offset: Int): Long {
//        val seconds = convertToLong(buffer, offset)
//        val fraction = convertToLong(buffer, offset + 4)
//        return (seconds - OFFSET_1900_TO_1970) * 1000 + fraction * 1000L / 0x100000000L
//    }


}