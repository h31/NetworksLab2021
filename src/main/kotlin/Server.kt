import Models.CustomSocket
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class Server constructor(private val host: String, private val port: Int) {
    private val selector = ActorSelectorManager(Dispatchers.IO)
    private val clientSockets = mutableMapOf<String, CustomSocket>()
    private lateinit var publicSocket : ServerSocket
    private var log = Logger.getLogger(Server::class.java.name)
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

        val isNicknameTaken = clientSockets.containsKey(nickname)
        val isNicknameIncorrect = !nickname.matches(nicknameRegex)
        val isNicknameServer = nickname.lowercase(Locale.getDefault()) == "server"

        customMessage.msg = when {
            isNicknameTaken -> "Sorry, this nickname ($nickname) is already taken. Choose another one."
            isNicknameIncorrect -> "Sorry, this nickname is incorrect. " +
                    "It can consist only of any combination of letters and digits."
            isNicknameServer -> "Sorry, any 'Server' nickname can not be taken. Choose another one."
            else -> "Hello, $nickname, you are connected!"
        }
        writer.writeFully(ByteBuffer.wrap(customMessage.toString().toByteArray()))
        if (isNicknameTaken || isNicknameIncorrect || isNicknameServer) {
            closeAll(writer, customSocket.aSocket)
        } else {
            clientSockets[nickname] = customSocket
            log.log(Level.FINE, "$nickname connected")
            clientSocketListener(nickname, customSocket)
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
            } catch (ex: Throwable) {
                log.log(Level.INFO, ex.localizedMessage)
                println("$nickname disconnected; ${clientSockets.keys.size} remains connected.")
                clientSockets.remove(nickname)
                return
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
        log.log(Level.FINE, "User $nickname disconnected; ${clientSockets.keys.size} remains connected.")
    }
}
