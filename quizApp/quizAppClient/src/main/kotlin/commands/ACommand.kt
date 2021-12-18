package commands

import io.ktor.client.features.*
import java.net.ConnectException

abstract class ACommand : Command {

    override suspend fun execute(): Boolean {
        return try {
            safeExecute()
        } catch (cause: ResponseException) {
            false
        } catch (ex: ConnectException) {
            false
        }
    }

    protected abstract suspend fun safeExecute(): Boolean
}