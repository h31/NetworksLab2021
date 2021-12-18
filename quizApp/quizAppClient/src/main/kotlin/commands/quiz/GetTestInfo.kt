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
import io.ktor.http.*
import model.Test

class GetTestInfo(private val httpClient: HttpClient, private val id: Int) : ACommand() {
    override suspend fun safeExecute(): Boolean {
        val response = httpClient.get<HttpResponse>(Routes.getUrl(Routes.TEST) + id)
        if (response.status != HttpStatusCode.OK) {
            echo("${response.receive<String>()}\n")
        }
        else {
            echo("${response.receive<Test>()}\n")
        }
        return false
    }
}