package commands.quiz

import auth.AuthService
import commands.Command
import io.ktor.client.*

class Logout(private val httpClient: HttpClient): Command {
    override suspend fun execute(): Boolean {
        httpClient.close()
        AuthService().start()
        return true
    }
}