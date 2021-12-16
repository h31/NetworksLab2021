package commands.quiz

import Routes
import com.github.ajalt.clikt.output.TermUi.echo
import commands.Command
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import model.User

class GetProfile(private val httpClient: HttpClient, private val username: String) : Command {
    override suspend fun execute(): Boolean {
        try {
            val response = httpClient.get<HttpResponse>(Routes.getUrl(Routes.PROFILE) + username)
            echo(response.receive<User>().toString())
        } catch (cause: ResponseException) {
            cause.response
        }
        return false
    }
}