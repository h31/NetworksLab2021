package com.monkeys.ntp.client

import com.monkeys.ntp.*
import com.monkeys.ntp.models.NtpPacket
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

class Client(private val host: String?, private val port: Int?) {

    fun start() {
        val socket = DatagramSocket()
        socket.use {
            val ntpPacket = ByteArray(NTP_PACKET_SIZE)
            val clientTime = ByteArray(8)

            try {
                val packet = DatagramPacket(ntpPacket, ntpPacket.size, InetAddress.getByName(host ?: "localhost"), port ?: NTP_PORT)
                val response = DatagramPacket(ntpPacket, ntpPacket.size)

                ntpPacket[0] = (NTP_VERSION.shl(3)).or(NTP_MODE_CLIENT).toByte()

                val requestTime = System.currentTimeMillis()
                writeTimeStamp(clientTime, requestTime)
                System.arraycopy(clientTime, 0, ntpPacket, TRANSMIT_OFFSET, 8)

                socket.send(packet)

                socket.receive(response)

                val responseArray = response.data
                val h = NtpPacket(responseArray)
                checkValidServerResponse(h.leapIndicator, h.versionNumber, h.mode, h.stratum)

                println("${Date(h.transmit)}\n\nPacket info:\n$h")

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}