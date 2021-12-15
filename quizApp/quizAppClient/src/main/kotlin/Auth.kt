import com.github.ajalt.clikt.output.TermUi.echo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.AuthData
import model.AuthSuccess
import java.util.*

class Auth {
    private lateinit var token: String
    private val scanner = Scanner(System.`in`)
    private val httpClient: HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    suspend fun start() {
        help()
        while (true) {
            val userInput = scanner.nextLine()
            val inputList = userInput.replace("\\s+".toRegex(), " ").trim().split(" ")
            when {
                inputList.size == 4 && inputList.first() == "/register" -> {
                    if (validatePwd(inputList[2], inputList[3]))
                        register(inputList[1], inputList[2])
                }
                inputList.size == 3 && inputList.first() == "/login" -> {
                    if (login(inputList[1], inputList[2])) {
                        QuizService(token, inputList[1]).start()
                        break
                    }
                }
                (inputList.size == 1 && inputList.first() == "/help") -> help()
                else -> echo("Incorrect command format, for more info use /help")
            }
        }
    }

    private suspend fun login(username: String, password: String): Boolean {
        val response = httpClient.post<HttpResponse>(Routes.LOGIN) {
            contentType(ContentType.Application.Json)
            body = AuthData(username, password)
        }
        return when (response.status) {
            HttpStatusCode.OK -> {
                val receive = response.receive<AuthSuccess>()
                token = receive.jwtToken
                true
            }
            HttpStatusCode.NotFound -> {
                echo(response.receive<String>())
                false
            }
            else -> {
                echo("Error code:${response.status.value}\nDescription:${response.status.description}")
                false
            }
        }
    }

    private suspend fun register(username: String, password: String): Boolean {
        val response = httpClient.post<HttpResponse>(Routes.REGISTER) {
            contentType(ContentType.Application.Json)
            body = AuthData(username, password)
        }
        return when (response.status) {
            HttpStatusCode.Created -> {
                echo(response.receive<String>())
                true
            }
            HttpStatusCode.BadRequest -> {
                echo(response.receive<String>())
                false
            }
            else -> {
                echo("Error code:${response.status.value}\nDescription:${response.status.description}")
                false
            }
        }
    }

    private fun validatePwd(password1: String, password2: String): Boolean {
        return when {
            password1.length < 7 -> {
                echo("Too small password")
                false
            }
            password1 != password2 -> {
                echo("Passwords not equals")
                false
            }
            else -> true
        }
    }

    private fun help() {
        echo(buildString {
            appendLine("Commands for Auth:")
            appendLine("    /register [username] [password] [repeat_password]")
            appendLine("    /login [username] [password]")
            appendLine("    /help")
        })
    }
}
