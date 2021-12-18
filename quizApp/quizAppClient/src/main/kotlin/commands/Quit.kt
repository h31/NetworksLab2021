package commands

import io.ktor.client.*

class Quit(private val httpClient: HttpClient): ACommand() {
    override suspend fun safeExecute(): Boolean {
        httpClient.close()
        return true
    }
}