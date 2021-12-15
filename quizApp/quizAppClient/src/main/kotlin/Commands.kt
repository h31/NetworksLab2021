import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import kotlinx.coroutines.runBlocking

class Commands: CliktCommand() {
    private val host by option("-h", "--host", help = "Set host address")
        .default(DEFAULT_HOST)
        .validate{
            require(isValidIP(it)) {
                "Incorrect host address (required IPv4 or IPv6 format)"
            }
        }
    private val port by option("-p", "--port", help = "Set port")
        .int()
        .restrictTo(0,0xffff)
        .default(DEFAULT_PORT)

    override fun run() {
        echo("Starting client...\nport: $port, host: $host")
        runBlocking {
            Auth().start()
        }
    }
}