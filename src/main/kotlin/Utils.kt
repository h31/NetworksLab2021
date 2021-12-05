import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.apache.commons.validator.routines.InetAddressValidator
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.util.*

const val ATTACHMENT_STRING = """att\|[^|]*\|"""
const val DEFAULT_HOST = "localhost"
const val DEFAULT_PORT = 5000
val nicknameRegex = Regex("""[а-яА-Я\w\d]+""")
val colonAndSpaceRegex = Regex(":\\s")

fun closeAll(writer : ByteWriteChannel, socket: ASocket) {
    writer.close()
    socket.close()
}

fun getLocalTime(timeUtc: String) = LocalTime.ofInstant(Instant.parse(timeUtc), ZoneId.systemDefault()).toString()

fun isValidIP(ip: String): Boolean {
    val validator = InetAddressValidator.getInstance()
    if (validator.isValid(ip) || ip.lowercase(Locale.getDefault()) == "localhost")
        return true
    return false
}

fun getMessage(reader: ByteReadChannel, fieldsAmount: Int): Flow<Pair<String, String>> = flow { // flow builder
    for (i in 0 until fieldsAmount) {
        val msg = reader.readUTF8Line()!!
        val split = msg.split(colonAndSpaceRegex, 2)
        val type = split.first().toString()
        val value = split.last().toString()
        emit(Pair(type, value))
    }
}


