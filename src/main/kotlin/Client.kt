import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.*
import java.io.BufferedInputStream
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


class Client constructor(hostAddress_: String, hostPort_: Int, private var nickname: String) {
    private val hostAddress = hostAddress_
    private val hostPort = hostPort_
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
            var msg: String
            for (i in 0 until 5) { //one time for every type
                try {
                    msg = aInput.readUTF8Line()!!
                }
                catch (ex: Exception) {
                    when(ex) {
                        is SocketException, is NullPointerException -> {
                            println("Server closed connection. Disconnected.")
                            exitProcess(0)
                        }
                        else -> throw ex
                    }
                }
                //parse the message!
                val split = msg.split(colonAndSpaceRegex, 2)
                val type = split.first().toString()
                val value = split.last().toString()
                when (type) {
                    "time" -> {customMsg.time = getLocalTime(value)}
                    "name" -> customMsg.name = value
                    "msg" -> customMsg.msg = value.replace("\\n","\n").replace("\\t","\t")
                    "attname" -> customMsg.attname = value
                    "att" -> customMsg.att = value
                }
            }
            //now customMsg have all the attributes. So we are...
            handleIncomingAtt(customMsg) //dealing with attachment if any exists...
            handleIncomingText(customMsg)
             //and showing the text message to the user
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
            if (text.toLowerCase(Locale.getDefault()) == "quit") {
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
                val file = File.createTempFile("media_", ".${File(attname).extension}", directory)
                customMsg.msg = customMsg.msg.replaceFirst(ATTACHMENT_STRING.toRegex(), "(file ${file.name} attached)")
                val len = att.toInt()
                val f = ByteArray(len)
                println("before")
                aInput.readFully(f, 0, len)
                println("after")
                file.writeBytes(f)
            }
            else -> {
                println("Incorrect message attachments - attachment can not be displayed.")
            }
        }
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
            var pathStr = matcher.group(0)
            pathStr = pathStr.substring(4, pathStr.length - 1) //try to get the insides...
            val file = File(pathStr) //and check if it is a correct path to file
            //if path is correct...
            if (file.isFile) {
                //check its mimeType to be image or video. Everything else is non-positive!
                val mimeType: String = withContext(CoroutineScope(Dispatchers.IO).coroutineContext) {
                    BufferedInputStream(FileInputStream(file)).use {
                        URLConnection.guessContentTypeFromStream(it)
                    }
                }

                if ((mimeType != "") && (mimeType.startsWith("image") || mimeType.startsWith("video"))) {
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
        if (f.isNotEmpty()) { aOutput.writeFully(f) }
    }
}