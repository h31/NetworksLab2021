import Models.CustomSocket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.net.SocketException
import java.nio.ByteBuffer
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class Server constructor(private val host: String, private val port: Int) {
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
            val customSocket = CustomSocket(publicSocket.accept())
            CoroutineScope(Dispatchers.IO).launch{ clientNicknameListener(customSocket) }
        }
    }

    private suspend fun clientNicknameListener(customSocket: CustomSocket) {
        val reader = customSocket.reader
        val writer = customSocket.writer
        val nickname = reader.readUTF8Line()!!
        val timeStr = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
        val customMessage = CustomMessage(timeStr, "Server")
        when {
            clientSockets.containsKey(nickname) -> {
                customMessage.msg = "Sorry, this nickname ($nickname) is already taken. Choose another one."
                writer.writeFully(ByteBuffer.wrap(customMessage.toString().toByteArray()))
                closeAll(writer, customSocket.aSocket)
            }
            !nickname.matches(nicknameRegex) -> {
                customMessage.msg = "Sorry, this nickname is incorrect. " +
                        "It can consist only of any combination of letters and digits."
                writer.writeFully(ByteBuffer.wrap(customMessage.toString().toByteArray()))
                closeAll(writer, customSocket.aSocket)
            }
            nickname.lowercase(Locale.getDefault()) == "server" -> {
                customMessage.msg = "Sorry, any 'Server' nickname can not be taken. Choose another one."
                writer.writeFully(ByteBuffer.wrap(customMessage.toString().toByteArray()))
                closeAll(writer, customSocket.aSocket)
            }
            else -> {
                customMessage.msg = "Hello, $nickname, you are connected!"
                writer.writeFully(ByteBuffer.wrap(customMessage.toString().toByteArray()))
                clientSockets[nickname] = customSocket
                println("$nickname connected")
                clientSocketListener(nickname, customSocket)
            }
        }
    }

    private suspend fun clientSocketListener(nickname: String, socket: CustomSocket) {
        val reader = socket.reader
        while (!socket.aSocket.isClosed) {
            //reading incoming message
            //can not use the same code from client because message format is different: here are 3 attrs, there are 5
            val customMsg = CustomMessage(nickname)
            val flow = getMessage(reader, 3)
            try {
                flow.collect { pair ->
                    when (pair.first) {
                        "msg" -> customMsg.msg = pair.second
                        "attname" -> customMsg.attname = pair.second
                        "att" -> customMsg.att = pair.second
                    }
                }
            } catch (ex: Exception) {
                when (ex) {
                    is SocketException, is NullPointerException -> {
                        clientSockets.remove(nickname)
                        println("$nickname disconnected; ${clientSockets.keys.size} remains connected.")
                        return
                    }
                    else -> {
                        ex.printStackTrace()
                    }
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

        //out of loop - close all...
        val customSocket = clientSockets[nickname]!!
        closeAll(customSocket.writer, customSocket.aSocket)
        clientSockets.remove(nickname) //...and remove socket from list of active clients
        println("User $nickname disconnected; ${clientSockets.keys.size} remains connected.")
    }
}
