import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.net.Socket
import java.nio.file.Paths
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.system.exitProcess

const val ATTACHMENT_STRING = """att\|[^|]*\|"""
val nicknameRegex = Regex("""[\w\d]+""")
val colonAndSpaceRegex = Regex(":\\s")

fun writeAndFlush(writer: BufferedWriter, message: String) {
    writer.write(message)
    writer.newLine()
    writer.flush()
}

fun checkPort(portIn: String): Int {
    val port = portIn.toIntOrNull()
    if (port == null) {
        println("incorrect port format. Port should be an integer.")
        exitProcess(0)
    }
    else if (!((port > 1024) and (port < 65535))) {
        println("Incorrect port format. Port should be in [1025, 65534] range.")
        exitProcess(0)
    }
    return port
}

fun closeAll(reader: BufferedReader, writer: BufferedWriter, socket: Socket) {
    reader.close()
    writer.close()
    socket.close()
}



