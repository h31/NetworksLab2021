import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.URLConnection
import java.nio.ByteBuffer
import java.nio.file.Paths
import java.util.*
import java.util.regex.Pattern
import kotlin.system.exitProcess

class Client constructor(private val hostAddress: String,
                         private val hostPort: Int,
                         private val nickname: String) {
    private val selector = ActorSelectorManager(Dispatchers.IO)
    private val scanner = Scanner(System.`in`, Charsets.UTF_8)
    private lateinit var aInput : ByteReadChannel
    private lateinit var aOutput : ByteWriteChannel

    fun run() = runBlocking {
        val aSocket = aSocket(selector).tcp().connect(InetSocketAddress(hostAddress, hostPort))
        aInput = aSocket.openReadChannel()
        aOutput = aSocket.openWriteChannel(autoFlush = true)
        aOutput.writeFully(ByteBuffer.wrap("$nickname\n".toByteArray()))
        launch(Dispatchers.IO) { handleIncoming(aSocket) }
        launch(Dispatchers.IO) { handleSent(aSocket) }
    }

    private suspend fun handleIncoming(aSocket: Socket) {
        while (!aSocket.isClosed) {
            //reading incoming message
            val customMsg = CustomMessage()
            val flow = getMessage(aInput, 5)
            try {
                flow.collect { pair ->
                    when (pair.first) {
                        "time" -> customMsg.time = getLocalTime(pair.second)
                        "name" -> customMsg.name = pair.second
                        "msg" -> customMsg.msg = pair.second.replace("\\n","\n").replace("\\t","\t")
                        "attname" -> customMsg.attname = pair.second
                        "att" -> customMsg.att = pair.second
                    }
                }
            } catch (ex: SocketException) {
                println("Server closed connection. Disconnected.")
                exitProcess(0)
            }
            //now customMsg have all the attributes. So we are...
            //dealing with attachment if any exists...
            handleIncomingAtt(customMsg)
        }
    }

    private suspend fun handleSent(aSocket: Socket) {
        while (!aSocket.isClosed) {
            //reading user input
            val text = scanner.nextLine()
            if (text.isBlank()) continue //no blank lines in msg!
            var msg = "msg: $text"
            msg = msg.replace("\n","\\n").replace("\t","\\t")

            //quit scenario with "quit" command
            if (text.lowercase(Locale.getDefault()) == "quit") {
                println("See you later. Bye!")
                exitProcess(0)
            }
            //check if message has any attachments - try to attach them if they exist, if not - send the msg itself
            handleSentAtt(msg)
        }
        //socket is closed - close everything that depends on it from client side
        closeAll(aOutput, aSocket)
    }

    private suspend fun handleIncomingAtt(customMsg: CustomMessage) {
        val att = customMsg.att
        val attname = customMsg.attname
        when {
            attname.isBlank() and att.isBlank() -> { /* can do something later, if there are any ideas */ }
            attname.isNotBlank() and att.isNotBlank() -> {
                val directory = File(Paths.get("").toAbsolutePath().toString() + "/images")
                if (!directory.exists()) directory.mkdir()
                val file = withContext(Dispatchers.IO) {
                    File.createTempFile("media_", ".${File(attname).extension}", directory)
                }
                val len = att.toInt()
                val f = ByteArray(len)
                aInput.readFully(f, 0, len)
                customMsg.msg = customMsg.msg.replaceFirst(ATTACHMENT_STRING.toRegex(),
                    "(file ${file.name} attached)")
                file.writeBytes(f)
            }
            else -> {
                println("Incorrect message attachments - attachment can not be displayed.")
            }
        }
        //and showing the text message to the user
        handleIncomingText(customMsg)
    }

    private fun handleIncomingText(customMsg: CustomMessage) {
        //easy access to output string format. Can also use String.format() instead
        println("<${customMsg.time}> [${customMsg.name}]: ${customMsg.msg}")
    }

    private suspend fun handleSentAtt(msg: String) {
        //check if there are any attachments (in "att|path/to/file.xxx|" format)
        var attname = ""
        var att = ""
        var f = byteArrayOf()

        val p = Pattern.compile(ATTACHMENT_STRING)
        val matcher = p.matcher(msg)
        if (matcher.find()) { //if we found "att|...|" ...
            val pathStr = matcher.group(1)
            val file = File(pathStr) //and check if it is a correct path to file
            //if path is correct...
            if (file.isFile) {
                //check its mimeType to be image or video. Everything else is non-positive!
                val mimeType: String? = withContext(Dispatchers.IO) {
                    FileInputStream(file).buffered()
                        .use(URLConnection::guessContentTypeFromStream)
                }

                if ((!mimeType.isNullOrBlank()) && (mimeType.startsWith("image") || mimeType.startsWith("video"))) {
                    f = file.readBytes()
                    att = f.size.toString()
                    attname = file.name
                }
                else {
                    println("WARNING: only images or video allowed as attachments.")
                }
            }
            else {
                println("WARNING: could not find file using given path.")
            }
        }
        val byteMsgOut = ByteBuffer.wrap(msg.plus("\nattname: $attname\natt: $att\n").toByteArray(Charsets.UTF_8))
        aOutput.writeFully(byteMsgOut)
        if (f.isNotEmpty()) aOutput.writeFully(f)
    }
}