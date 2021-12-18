package commands

import Routes
import auth.AuthService
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.prompt
import com.github.ajalt.clikt.parameters.options.validate
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.restrictTo
import kotlinx.coroutines.runBlocking
import java.net.InetAddress
import java.net.UnknownHostException

class StartupCommands: CliktCommand() {
    private val host by option("-h", "--host", help = "Set host address")
        .prompt("Enter host address")
        .validate{
            require(validateIp(it)) {
                "Incorrect host address (required IPv4, IPv6 format or domain name)"
            }
        }
    private val port by option("-p", "--port", help = "Set port")
        .int()
        .restrictTo(0,0xffff)
        .prompt("Enter host port")

    override fun run() {
        echo("Starting client...\nport: $port, host: $host")
        runBlocking {
            Routes.HOST = host
            Routes.PORT = port
            AuthService().start()
        }
    }

    //Stackoverflow
    private fun validateIp(ip: String): Boolean {
        return try {
            InetAddress.getByName(ip)
            true
        } catch (ex: UnknownHostException) {
            false
        }
    }
}