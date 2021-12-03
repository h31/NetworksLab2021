import Models.LoadConfig
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.groupChoice
import com.github.ajalt.clikt.parameters.groups.required
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import java.io.IOException

class Commands : CliktCommand(printHelpOnEmptyArgs = true) {
    private val type : LoadConfig by option("-t", "--type", help = "Choose to start server or client")
        .groupChoice(
        "client" to LoadConfig.ClientType(),
        "server" to LoadConfig.ServerType())
        .required()

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
        when(val it = type) {
            is LoadConfig.ClientType -> {
                echo("Starting client...\nport: $port, host: $host, nickname: ${it.nickname}")
                try {
                    Client(host, port, it.nickname).run()
                } catch (ex : IOException) {
                    echo("Can not connect to server.")
                }
            }
            is LoadConfig.ServerType -> {
                echo("Starting server...\nport: $port, host: $host")
                try {
                    Server(host, port).run()
                } catch (ex: IOException) {
                    echo("Can not create server socket. Maybe the port is already taken?")
                }
            }
        }
    }

}