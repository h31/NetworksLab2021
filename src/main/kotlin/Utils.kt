import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
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
    else if (!((port >= 0) and (port < 65535))) {
        println("Incorrect port format. Port should be in [0, 65534] range.")
        exitProcess(0)
    }
    return port
}

fun closeAll(reader: BufferedReader, writer: BufferedWriter, socket: Socket) {
    reader.close()
    writer.close()
    socket.close()
}

fun getLocalTime(timeUtc: String) = LocalTime.ofInstant(Instant.parse(timeUtc), ZoneId.systemDefault()).toString()



