package commands

import io.ktor.client.*

class Quit(private val httpClient: HttpClient): Command {
    override suspend fun execute(): Boolean {
        httpClient.close()
        return true
    }
}