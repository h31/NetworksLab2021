import models.*
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.*
import kotlin.system.exitProcess

class Client {
    private val scanner = Scanner(System.`in`)

    fun run() {
        println("Enter the type (A, AAAA, MX, TXT)")
        val type: RecordType = when (scanner.nextLine().toUpperCase()) {
            "A" -> RecordType.of(1)
            "AAAA" -> RecordType.of(28)
            "MX" -> RecordType.of(15)
            "TXT" -> RecordType.of(16)
            else -> {
                println("This record type is not implemented")
                exitProcess(1)
            }
        }

        println("Enter the domain name you wanna find info about")
        val domainName = scanner.nextLine().toLowerCase()

        println("Enter the ip address of the dns server you wanna connect to")
        val ipText = scanner.nextLine()

        println("Enter the port of dns server or leave empty for default (53)")
        var port = PORT
        var portText = scanner.nextLine()
        if (portText.isBlank()) portText = "53"

        if (ipText.isBlank() || domainName.isBlank()) {
            println("One of the field was empty.")
            exitProcess(1)
        }

        try { port = portText.toInt() }
        catch (ex: NumberFormatException) {
            println("Incorrect port format. Used default port: $port")
        }
        send(ipText, port, domainName, type)
    }

    private fun send(ipText: String, port: Int, domainName: String, type: RecordType) {
        val addr: InetSocketAddress
        try {
            addr = InetSocketAddress(ipText, port)
        } catch (e: IllegalArgumentException) {
            println("IP/port combination is incorrect ($ipText:$port)")
            exitProcess(1)
        }

        val reqId = rndShort()

        val header = Header(id = reqId)
        val question = Question(qname = domainName, qtype = type, qclass = RecordClass.of(1))
        val dnsMsg = DNSMessage(header, question, listOf())

        val sendData = dnsMsg.toByteArray()
        val socket = DatagramSocket()
        socket.use { datagramSocket ->
            datagramSocket.connect(addr.address, addr.port)
            val packet = DatagramPacket(sendData, sendData.size, addr.address, addr.port)
            datagramSocket.send(packet)

            val receiveBuf = ByteArray(MAX_PACKET_SIZE)
            val response = DatagramPacket(receiveBuf, receiveBuf.size)
            datagramSocket.receive(response)

            val data = response.data
            val retDNSMessage = DNSMessage.parseByteArray(data)
            if (retDNSMessage.resList.isNotEmpty()) {
                var i = 0
                val res = retDNSMessage.resList.size
                println("answers = $res")
                println("anCount = ${retDNSMessage.header.ancount}")
                for (j in 0 until retDNSMessage.header.ancount) {
                    println("\t${retDNSMessage.resList[i].name} : ${retDNSMessage.resList[i].rdata}")
                    i++
                }
                println("nsCount = ${retDNSMessage.header.nscount}")
                for (j in 0 until retDNSMessage.header.nscount) {
                    println("\t${retDNSMessage.resList[i].name} : ${retDNSMessage.resList[i].rdata}")
                    i++
                }
                println("arCount = ${retDNSMessage.header.arcount}")
                for (j in 0 until retDNSMessage.header.arcount) {
                    println("\t${retDNSMessage.resList[i].name} : ${retDNSMessage.resList[i].rdata}")
                    i++
                }
            }
            else println("No results")
        }
    }
}