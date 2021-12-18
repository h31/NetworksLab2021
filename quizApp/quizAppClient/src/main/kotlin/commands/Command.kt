package commands

interface Command {
    suspend fun execute(): Boolean
}