package commands.auth

import com.github.ajalt.clikt.output.TermUi.echo
import commands.ACommand
import commands.Command
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.AuthData
import model.AuthSuccess
import quiz.QuizService
import java.net.ConnectException

class Login(private val httpClient: HttpClient,
            private val username: String,
            private val password: String) : ACommand() {

    override suspend fun safeExecute(): Boolean {
        val response = httpClient.post<HttpResponse>(Routes.getUrl(Routes.LOGIN)) {
            contentType(ContentType.Application.Json)
            body = AuthData(username, password.hashCode().toString(radix = 16))
        }
        return if (response.status != HttpStatusCode.OK) {
            echo("${response.receive<String>()}\n")
            false
        } else {
            val receive = response.receive<AuthSuccess>()
            val token = receive.jwtToken
            val newClient = httpClient.config {
                defaultRequest {
                    header(HttpHeaders.Authorization, "bearer $token")
                    contentType(ContentType.Application.Json)
                }
            }
            QuizService(username, newClient).start()
            true
        }
    }
}