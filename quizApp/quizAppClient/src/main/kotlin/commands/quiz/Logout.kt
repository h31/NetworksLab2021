package commands.quiz

import auth.AuthService
import commands.ACommand
import io.ktor.client.*

class Logout(private val httpClient: HttpClient): ACommand() {
    override suspend fun safeExecute(): Boolean {
        httpClient.close()
        AuthService().start()
        return true
    }
}