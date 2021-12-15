import com.github.ajalt.clikt.output.TermUi.echo
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.*
import java.util.*

class QuizService(private val token: String, private val username: String) {

    private val scanner = Scanner(System.`in`)
    private val httpClient = HttpClient(CIO) {
        HttpResponseValidator {
            handleResponseException { exception ->
                if (exception !is ClientRequestException) return@handleResponseException
                val exceptionResponse = exception.response
                when (exceptionResponse.status) {
                    HttpStatusCode.BadRequest -> echo(exceptionResponse.readText())
                    HttpStatusCode.NotFound -> echo(exceptionResponse.readText())
                    else -> echo("Error code:${exceptionResponse.status.value}\n" +
                            "Description:${exceptionResponse.status.description}")
                }
            }
        }
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        defaultRequest {
            header(HttpHeaders.Authorization, "bearer $token")
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun start() {
        echo("Login Successful")
        help()
        while (true) {
            val userInput = scanner.nextLine()
            val inputList = userInput.replace("\\s+".toRegex(), " ").trim().split(" ")
            when (inputList[0].lowercase(Locale.getDefault())) {
                "/test" -> {
                    when (inputList.size) {
                        1 -> getTests()
                        2 -> getTestInfo(inputList[1])
                        else -> errorMsg(inputList[0])
                    }
                }
                "/profile" -> {
                    if (inputList.size != 1) errorMsg(inputList[0])
                    else profile(username)
                }
                "/start" -> {
                    if (inputList.size != 2) errorMsg(inputList[0])
                    else startTest(inputList[1])
                }
                "/help" -> help()
                "/quit" -> break
                else -> errorMsg("Unrecognized command, use \"/help\" for more info")
            }
        }
    }

    private suspend fun getTestInfo(id: String) {
        val testId = id.toIntOrNull() ?: return echo("ID not a number")
        try {
            val response = httpClient.get<HttpResponse>(Routes.GET_TEST + testId)
            echo(response.receive<Test>().toString())
        } catch (cause: ResponseException) {
            cause.response
        }
    }

    private suspend fun profile(username: String) {
        try {
            val response = httpClient.get<HttpResponse>(Routes.PROFILE + username)
            echo(response.receive<User>().toString())
        } catch (cause: ResponseException) {
            cause.response
        }
    }

    private suspend fun getTests() {
        try {
            val response = httpClient.get<TestsList>(Routes.GET_TESTS)
            echo(response.toString())
        } catch (cause: ResponseException) {
            cause.response
        }
    }

    private suspend fun startTest(id: String) {
        val testId = id.toIntOrNull() ?: return echo("ID not a number")
        try {
            val response = httpClient.get<HttpResponse>(Routes.GET_QUESTIONS + "/$testId")
            showTest(response.receive(), testId)
        } catch (cause: ResponseException) {
            cause.response
        }
    }

    private suspend fun showTest(questionsList: QuestionsList, id: Int) {
        val answers = mutableListOf<Int>()
        questionsList.questionsList.forEach { question ->
            echo(question.getQuestion())
            while (true) {
                val answer = scanner.nextLine()
                if (!validateAnswer(answer)) {
                    echo("Incorrect answer format!!! Number in range 1..4 excepted.")
                } else {
                    answers.add(answer.toInt())
                    break
                }
            }
        }
        try {
            val response = httpClient.post<HttpResponse>(Routes.GET_RESULT) {
                body = Answers(answers.toList(), id, username)
            }
            val result = response.receive<AnswersResult>()
            echo("Yours result: ${result.resultSum}")
        } catch (cause: ResponseException) {
            cause.response
        }
    }

    private fun errorMsg(s: String) {
        echo("Incorrect format for command \"$s\", use \"/help\" for more info")
    }

    private fun validateAnswer(answer: String): Boolean {
        if (answer.isNotBlank()) {
            val number = answer.toIntOrNull() ?: return false
            return number in 1..4
        }
        else return false
    }

    private fun help() = echo(buildString {
        appendLine("Commands:")
        appendLine("    /test           --> print all available tests")
        appendLine("    /profile        --> print info about user")
        appendLine("    /test [id]      --> print info about test with [id]")
        appendLine("    /start [id]     --> start test with such [id]")
        appendLine("    /help           --> prints this message")
        appendLine("    /quit           --> quit from app")
    })
}