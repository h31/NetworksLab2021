package auth

import commands.auth.AuthCommands
import commands.auth.Login
import commands.auth.Register
import com.github.ajalt.clikt.output.TermUi.echo
import commands.Quit
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.util.*

class AuthService {
    private val scanner = Scanner(System.`in`)
    private val helpAuthMsg = buildString {
        appendLine("Commands for Auth:")
        appendLine("    /register [username] [password] [repeat_password]   --> register profile in service")
        appendLine("    /login [username] [password]                        --> connect to service")
        appendLine("    /help                                               --> prints this message")
        appendLine("    /quit                                               --> quit from app")
    }
    private var httpClient: HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        HttpResponseValidator {
            handleResponseException { exception ->
                if (exception !is ClientRequestException) return@handleResponseException
                val exceptionResponse = exception.response
                when (exceptionResponse.status) {
                    HttpStatusCode.BadRequest -> echo(exceptionResponse.readText())
                    HttpStatusCode.NotFound -> echo(exceptionResponse.readText())
                    else -> echo("Error code:${exceptionResponse.status.value}\n" +
                            "Description:${exceptionResponse.status.description}")
                }
            }
        }
    }

    suspend fun start() {
        echo(helpAuthMsg)
        while (true) {
            val userInput = scanner.nextLine()
            val inputList = userInput.trim().split("\\s+".toRegex())
            val command = AuthCommands.findByString(inputList[0].lowercase())
            if (command == null) {
                echo("Unrecognized command, use \"/help\" for more info\n")
                continue
            }
            if (inputList.size == command.argsSize) {
                when (command) {
                    AuthCommands.REGISTER -> {
                        Register(httpClient, inputList[1], inputList[2], inputList[3]).execute()
                    }
                    AuthCommands.HELP -> {
                        echo(helpAuthMsg)
                    }
                    AuthCommands.LOGIN -> {
                        if (Login(httpClient, inputList[1], inputList[2]).execute())
                            break
                    }
                    AuthCommands.QUIT -> {
                        Quit(httpClient).execute()
                        break
                    }
                }
            }
            else {
                echo("Incorrect format for command \"${inputList[0]}\", use \"/help\" for more info\n")
            }
        }
    }
}
