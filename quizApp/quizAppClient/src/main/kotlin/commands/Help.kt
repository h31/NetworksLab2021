package commands

import io.ktor.client.*

class Help(private val httpClient: HttpClient): ACommand() {
    override suspend fun safeExecute(msg: String): Boolean {
        echo(msg)
        return false
    }
}