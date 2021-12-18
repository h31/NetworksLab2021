package commands.quiz

import Routes
import com.github.ajalt.clikt.output.TermUi.echo
import commands.ACommand
import commands.Command
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import model.User

class GetProfile(private val httpClient: HttpClient, private val username: String) : ACommand() {
    override suspend fun safeExecute(): Boolean {
        val response = httpClient.get<HttpResponse>(Routes.getUrl(Routes.PROFILE) + username)
        echo(response.receive<User>().toString())
        return false
    }
}