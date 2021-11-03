package com.monkeys.ntp.client

import com.monkeys.ntp.*
import com.monkeys.ntp.models.Header
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

class Client(val host: String, val port: Int) {

    fun start() {
        val socket = DatagramSocket()
        socket.use {
            println("Client started!!!")

            val ntpPacket = ByteArray(NTP_PACKET_SIZE)
            val clientTime = ByteArray(8)

            val packet = DatagramPacket(ntpPacket, ntpPacket.size, InetAddress.getByName("localhost"), 4445)
            //val packet = DatagramPacket(ntpPacket, ntpPacket.size, InetAddress.getByName("pool.ntp.org"), NTP_PORT)
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

            println(Date(h.transmit))
        }

    }

}