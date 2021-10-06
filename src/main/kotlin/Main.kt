import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.time.Instant
import java.time.temporal.ChronoUnit

fun main(args: Array<String>) {
    when {
        (args.size == 1 || args.size == 2) and (args[0] == "-s") -> {
            try {
                val port = if (args.size == 1) 9876 else checkPort(args[1])
                val server = Server(port)
                runBlocking { server.run() }
            }
            catch (ex: IOException) {
                println("Can not create server socket. Maybe the port is already taken?")
            }
        }
        (args.size == 4) and (args[0] == "-c") -> {
            val addr = args[1]
            val port = checkPort(args[2])
            val nickname = args[3]
            try {
                val client = Client(addr, port, nickname)
                runBlocking { client.run() }
            }
            catch (ex : IOException) {
                println("Can not connect to server.")
            }
        }
        else -> {
            println("Unknown combination. Try [-s port] for server or [-c ip port nickname] for client.")
        }
    }
}