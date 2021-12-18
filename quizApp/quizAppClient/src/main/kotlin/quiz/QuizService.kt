package quiz

import com.github.ajalt.clikt.output.TermUi.echo
import commands.Help
import commands.Quit
import commands.quiz.*
import io.ktor.client.*
import java.util.*

class QuizService(private val username: String, private val httpClient: HttpClient) {

    private val scanner = Scanner(System.`in`)
    private val helpQuizMsg = buildString {
        appendLine("Commands:")
        appendLine("    /tests          --> print all available tests")
        appendLine("    /profile        --> print info about user")
        appendLine("    /testinfo [id]  --> print info about test with [id]")
        appendLine("    /start [id]     --> start test with such [id]")
        appendLine("    /logout         --> logout from profile")
        appendLine("    /help           --> prints this message")
        appendLine("    /quit           --> quit from app")
    }

    suspend fun start() {
        echo("Login Successful")
        echo(helpQuizMsg)
        while (true) {
            val userInput = scanner.nextLine()
            val inputList = userInput.trim().split("\\s+".toRegex())
            val command = TestCommands.findByString(inputList[0].lowercase())
            if (command == null) {
                echo("Unrecognized command, use \"/help\" for more info\n")
                continue
            }
            if (inputList.size == command.argsSize) {
                when (command) {
                    TestCommands.TEST_INFO -> {
                        val id = inputList[1].toIntOrNull()
                        if (id != null) GetTestInfo(httpClient, id).execute()
                        else echo("ID: \"${inputList[1]}\" - not a number")
                    }
                    TestCommands.START_TEST -> {
                        val id = inputList[1].toIntOrNull()
                        if (id != null) StartTest(httpClient, inputList[1], username).execute()
                        else echo("ID: \"${inputList[1]}\" - not a number")
                    }
                    TestCommands.GET_TESTS -> {
                        GetTests(httpClient).execute()
                    }
                    TestCommands.PROFILE -> {
                        GetProfile(httpClient, username).execute()
                    }
                    TestCommands.HELP -> {
                        Help(helpQuizMsg).execute()
                    }
                    TestCommands.QUIT -> {
                        Quit(httpClient).execute()
                        break
                    }
                    TestCommands.LOGOUT -> {
                        Logout(httpClient).execute()
                        break
                    }
                }
            } else {
                echo("Incorrect format for command \"${inputList[0]}\", use \"/help\" for more info")
            }
        }
    }
}