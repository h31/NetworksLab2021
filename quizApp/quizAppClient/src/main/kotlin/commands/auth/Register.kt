package commands.auth

import com.github.ajalt.clikt.output.TermUi.echo
import commands.Command
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.AuthData

class Register(private val httpClient: HttpClient,
               private val username: String,
               private val password: String,
               private val password2: String) : Command {
    override suspend fun execute(): Boolean {
        if (validatePwd(password, password2)) {
            try {
                val response = httpClient.post<HttpResponse>(Routes.getUrl(Routes.REGISTER)) {
                    contentType(ContentType.Application.Json)
                    body = AuthData(username, password)
                }
                echo("${response.receive<String>()}\n")
            } catch (cause: ResponseException) {
                cause.response
            }
        }
        return false
    }

    private fun validatePwd(password1: String, password2: String): Boolean {
        return when {
            password1.length < 7 -> {
                echo("Too small password\n")
                false
            }
            password1 != password2 -> {
                echo("Passwords not equals\n")
                false
            }
            else -> true
        }
    }
}