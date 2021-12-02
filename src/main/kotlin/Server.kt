import Models.CustomSocket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.ByteBuffer
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class Server constructor(host_: String, port_: Int) {
    private val host = host_
    private val port = port_

    private val selector = ActorSelectorManager(Dispatchers.IO)
    private val clientSockets = mutableMapOf<String, CustomSocket>()
    private lateinit var publicSocket : ServerSocket

    fun run() = runBlocking {
        publicSocket = aSocket(selector).tcp().bind(InetSocketAddress(host, port))
        println("This is your port, let the clients connect to it: $port")
        publicSocketListener(publicSocket)
    }

    private suspend fun publicSocketListener(publicSocket: ServerSocket) {
        while (true) {
            try {
                val customSocket = CustomSocket(publicSocket.accept())
                CoroutineScope(Dispatchers.IO).launch{ clientNicknameListener(customSocket) }
            } catch (e: IOException) {
                println("hi")
            }
        }
    }

    private suspend fun clientNicknameListener(customSocket: CustomSocket) {
        val reader = customSocket.reader
        val writer = customSocket.writer
        val nickname = reader.readUTF8Line()!!
        val timeStr = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
        val customMessage = CustomMessage(timeStr, "Server")
        if (clientSockets.containsKey(nickname)) {
            customMessage.msg = "Sorry, this nickname is already taken. Choose another one."
            writer.writeFully(ByteBuffer.wrap(customMessage.toString().toByteArray()))
            closeAll(writer, customSocket.aSocket)
        }
        else {
            customMessage.msg = "Hello, $nickname, you are connected!"
            writer.writeFully(ByteBuffer.wrap(customMessage.toString().toByteArray()))
            clientSockets[nickname] = customSocket
            println("$nickname connected")
            clientSocketListener(nickname, customSocket)
        }
    }

    private suspend fun clientSocketListener(nickname: String, socket: CustomSocket) {
        val reader = socket.reader
        while (!socket.aSocket.isClosed) {
            val customMsg = CustomMessage(nickname)
            var msg: String
            for (i in 0 until 3) { //one time for every type
                try {
                    msg = reader.readUTF8Line()!!
                } catch (ex: Exception) {
                    when (ex) {
                        is SocketException, is NullPointerException -> {
                            clientSockets.remove(nickname)
                            println("$nickname disconnected; ${clientSockets.keys.size} remains connected.")
                            return
                        }
                        else -> throw ex
                    }
                }
                //parse the message!
                val split = msg.split(colonAndSpaceRegex, 2)
                val type = split.first().toString()
                val value = split.last().toString()
                when (type) {
                    "msg" -> customMsg.msg = value
                    "attname" -> customMsg.attname = value
                    "att" -> customMsg.att = value
                }
            }

            //quit case - break out of loop, then close the stuff...
            if (customMsg.msg.lowercase(Locale.getDefault()) == "quit")
                break

            //dealing with attachment if any exists
            val len = if (customMsg.att.isBlank()) 0 else customMsg.att.toInt()
            val f = ByteArray(len)
            if (len > 0)
                reader.readFully(f, 0, len)

            //add the time and other attrs to customMsg
            val timeStr = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
            customMsg.time = timeStr
            customMsg.name = nickname
            customMsg.att = customMsg.att

            clientSockets.forEach {
                val writer = it.value.writer
                val msgByteArray = customMsg.toString().toByteArray(Charsets.UTF_8)
                writer.writeFully(ByteBuffer.wrap(msgByteArray))
                if (f.isNotEmpty()) writer.writeFully(f)
            }
        }
        //reading incoming message
        //can not use the same code from client because message format is different: here are 3 attrs, there are 5

        //out of loop - close all...
        val customSocket = clientSockets[nickname]!!
        closeAll(customSocket.writer, customSocket.aSocket)
        clientSockets.remove(nickname) //...and remove socket from list of active clients
        println("User $nickname disconnected; ${clientSockets.keys.size} remains connected.")
    }
}
