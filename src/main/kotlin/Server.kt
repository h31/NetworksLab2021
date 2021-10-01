import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

class Server constructor(port: Int) {
    private val  publicSocket: ServerSocket = ServerSocket(port)
    private val clientSockets = mutableMapOf<String, CustomSocket>()
    private val clientScope = CoroutineScope(Dispatchers.IO)

    suspend fun run() = coroutineScope {
        println("This is your port, let the clients connect to it: ${publicSocket.localPort}")
        launch (Dispatchers.IO) { publicSocketListener() }
    }

    private fun publicSocketListener() {
        while (true) {
            try {
                val customSocket = CustomSocket(publicSocket.accept())
                val reader = customSocket.reader
                val writer = customSocket.writer
                val nickname = reader.readLine()
                val timeStr = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
                val customMessage = CustomMessage(timeStr, "Server")
                when {
                    !nickname.matches(nicknameRegex) -> {
                        customMessage.msg = "Sorry, this nickname is incorrect. " +
                                "It can consist only of any combination of letters and digits."
                        writeAndFlush(writer, customMessage.toString())
                        closeAll(reader, writer, customSocket.socket)
                    }
                    clientSockets.containsKey(nickname) -> {
                        customMessage.msg = "Sorry, this nickname is already taken. Choose another one."
                        writeAndFlush(writer, customMessage.toString())
                        closeAll(reader, writer, customSocket.socket)
                    }
                    nickname.lowercase(Locale.getDefault()) == "server" -> {
                        customMessage.msg = "Sorry, any 'Server' nickname can not be taken. Choose another one."
                        writeAndFlush(writer, customMessage.toString())
                        closeAll(reader, writer, customSocket.socket)
                    }
                    else -> {
                        customMessage.msg = "Hello, $nickname, you are connected!"
                        writeAndFlush(writer, customMessage.toString())
                        clientSockets[nickname] = customSocket
                        println("$nickname connected")
                        clientScope.launch { clientSocketListener(nickname, customSocket) }
                    }
                }
            } catch (e:IOException) {
                println("Someone tried to connect, but unsuccessfully.")
            }
        }
    }

    private fun clientSocketListener(nickname: String, socket: CustomSocket) {
        val inStream = BufferedInputStream(socket.socket.getInputStream())
        while (socket.socket.isConnected) {
            //reading incoming message
            //can not use the same code from client because message format is different: here are 3 attrs, there are 5
            val customMsg = CustomMessage(nickname)
            var msg: String
            for (i in 0 until 3) { //one time for every type
                try { msg = socket.reader.readLine() }
                catch (ex: Exception) {
                    when(ex) {
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
            if (customMsg.msg.lowercase(Locale.getDefault()) == "quit") { break }

            //dealing with attachment if any exists
            val len = if (customMsg.att.isBlank()) 0 else customMsg.att.toInt()
            var f = ByteArray(len)
            if (len > 0) { f = inStream.readNBytes(len) }

            //add the time and other attrs to customMsg
            val timeStr = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString()
            customMsg.time = timeStr
            customMsg.name = nickname
            customMsg.att = customMsg.att + "\n"

            //two writing scenarios...
            //if there is no attachment...
            if (f.isEmpty()) {
                clientSockets.forEach {
                    val outStream = BufferedOutputStream(it.value.socket.getOutputStream())
                    outStream.write(customMsg.toString().toByteArray())
                    outStream.flush()
                }
            }
            //and if some exists
            else {
                clientSockets.forEach {
                    clientScope.launch (Dispatchers.IO) {
                        val outStream = BufferedOutputStream(it.value.socket.getOutputStream())
                        outStream.write(customMsg.toString().toByteArray())
                        outStream.flush()
                        outStream.write(f)
                        outStream.flush()
                    }
                }
            }
        }
        //out of loop - close all...
        val customSocket = clientSockets[nickname]!!
        closeAll(customSocket.reader, customSocket.writer, customSocket.socket)
        clientSockets.remove(nickname) //...and remove socket from list of active clients
        println("User $nickname disconnected; ${clientSockets.keys.size} remains connected.")
    }

    data class CustomSocket constructor (var socket: Socket) {
        var reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        var writer = BufferedWriter(OutputStreamWriter(socket.getOutputStream()))
    }
}