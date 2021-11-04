import models.*
import java.io.File
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.SocketException
import kotlin.system.exitProcess

class Server(private val serverPort: Int) {
    fun run() {
        val receiveBuf = ByteArray(MAX_PACKET_SIZE)
        val packet = DatagramPacket(receiveBuf, receiveBuf.size)
        val socket: DatagramSocket
        try {
            socket = DatagramSocket(serverPort)
        } catch (e: SocketException) {
            println("Can not open the socket at chosen port ($serverPort)")
            exitProcess(1)
        }
        while (true) {
            println("waiting...")
            socket.receive(packet)
            println("msg received!")
            val retAddress = packet.address
            val retPort = packet.port
            val dnsMsg = DNSMessage.parseByteArray(packet.data)
            dnsMsg.header.flags.qr = true //sending an answer

            var resources = listOf<Resource>()
            val responseCode = checkHeader(dnsMsg)
            if (responseCode == ResponseCode.of(0)) {
                val type = dnsMsg.question.qtype
                val qName = dnsMsg.question.qname
                val clazz = dnsMsg.question.qclass
                val ttl = 6000
                resources = getResource(qName, type, clazz, ttl)
                if (resources.isNotEmpty()) {
                    dnsMsg.header.ancount = countResources(resources, type).toShort()
                    dnsMsg.header.arcount = (resources.size - dnsMsg.header.ancount).toShort()
                }
                else errorFunc(dnsMsg, ResponseCode.of(3))
            }
            else errorFunc(dnsMsg, responseCode)

            dnsMsg.resList = resources
            val sentData = dnsMsg.toByteArray()
            val response = DatagramPacket(sentData, sentData.size, retAddress, retPort)
            socket.send(response)

            println("msg sent!")
        }
    }

    private fun countResources(res: List<Resource>, type: RecordType) = res.count { it.type == type }

    private fun getResource(name: String, rType: RecordType, rClass: RecordClass, ttl: Int): List<Resource>  {
        val result = mutableListOf<Resource>()
        val filePath =
            when (rType) {
            is RecordType.A -> RECORD_FILE_PATH + RecordType.A().toString()
            is RecordType.MX -> RECORD_FILE_PATH + RecordType.MX(0).toString()
            is RecordType.TXT -> RECORD_FILE_PATH + RecordType.TXT(0).toString()
            is RecordType.AAAA -> RECORD_FILE_PATH + RecordType.AAAA().toString()
            is RecordType.NotImpl -> return result
        }
        //types: 1 15 16 28
        //resource: name type rClass=1 ttl rdlength rdata
        val file = File(filePath)
        val res = mutableMapOf<String, RecordType>()
        file.readLines()
            .map { it.split(SPACE_CHARACTER.toRegex(), 2) }
            .filter { it[0] == name }
            .forEach { res[it[1]] = rType }

        for (entry in res) {
            val rdLength: Int = when(rType) {
                is RecordType.A -> RecordType.A().size()
                is RecordType.MX -> (entry.key.split(COLON_CHARACTER).last()).length + 4
                is RecordType.AAAA -> RecordType.AAAA().size()
                is RecordType.TXT -> entry.key.length
                is RecordType.NotImpl -> throw exceptions.NotImplTypeException(NOT_IMPL_MSG)
            }
            result.add(Resource(name, rType, rClass, ttl, rdLength.toShort(), entry.key))
        }
        val additionalRes = mutableListOf<Resource>()
        for (resource in result) {
            if (resource.type == RecordType.of(15)) {
                val resName = resource.rdata.split(COLON_CHARACTER).last()
                val resA = getResource(resName, RecordType.of(1), rClass, ttl)
                val resAAAA = getResource(resName, RecordType.of(28), rClass, ttl)
                additionalRes.addAll(resA)
                additionalRes.addAll(resAAAA)
            }
        }
        result.addAll(additionalRes)
        return result
    }

    private fun checkHeader(dnsMsg: DNSMessage): ResponseCode {
        val header = dnsMsg.header
        val flags = header.flags
        if (!flags.qr || flags.aa || flags.tc || flags.z.toInt() != 0
            || flags.rcode.code.toInt() != 0 || header.qdcount.toInt() == 0) {
            errorFunc(dnsMsg, ResponseCode.of(1))
            return ResponseCode.of(1)
        }
        else if (flags.opcode.code.toInt() != 0 || header.qdcount.toInt() > 1) {
            errorFunc(dnsMsg, ResponseCode.of(4))
            return ResponseCode.of(4)
        }
        return ResponseCode.of(0)
    }

    private fun errorFunc(dnsMsg: DNSMessage, rCode: ResponseCode) {
        dnsMsg.header.flags.rcode = rCode
    }
}