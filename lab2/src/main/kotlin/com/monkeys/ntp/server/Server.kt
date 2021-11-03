package com.monkeys.ntp.server

import com.monkeys.ntp.NTP_PACKET_SIZE
import com.monkeys.ntp.models.NtpPacket
import kotlinx.coroutines.runBlocking
import java.net.DatagramPacket
import java.net.DatagramSocket

class Server() {
    private val socket = DatagramSocket(4445)
    fun start() {
        socket.use {
            while (!socket.isClosed) {
                val ntpPacket = ByteArray(NTP_PACKET_SIZE)
                val packet = DatagramPacket(ntpPacket, ntpPacket.size)
                socket.receive(packet)
                println("sending time for new client")
                val receiveTime = System.currentTimeMillis()
                runBlocking {
                    getTimeForClient(packet, receiveTime)
                }
            }
        }
    }

    private fun getTimeForClient(inputPacket: DatagramPacket, receiveTime: Long) {
        val respond = NtpPacket(
            mode = 4,
            stratum = 2,
            precision = -25,
            rootDispersion = 2,
            refId = 1347441408,
            reference = receiveTime,
            originate = NtpPacket(inputPacket.data).transmit,
            receive = receiveTime
        )
        val ntpPacket = respond.toByteArray()
        socket.send(DatagramPacket(ntpPacket, ntpPacket.size,inputPacket.address, inputPacket.port))
    }
}