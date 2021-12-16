package commands.quiz

import Routes
import com.github.ajalt.clikt.output.TermUi.echo
import commands.Command
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import model.TestsList

class GetTests(private val httpClient: HttpClient) : Command {
    override suspend fun execute(): Boolean {
        try {
            val response = httpClient.get<TestsList>(Routes.getUrl(Routes.TESTS))
            echo(response.toString())
        } catch (cause: ResponseException) {
            cause.response
        }
        return false
    }
}