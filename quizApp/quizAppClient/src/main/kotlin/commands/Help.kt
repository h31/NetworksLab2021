package commands

import com.github.ajalt.clikt.output.TermUi.echo

class Help(private val msg: String): ACommand() {
    override suspend fun safeExecute(): Boolean {
        echo(msg)
        return false
    }
}