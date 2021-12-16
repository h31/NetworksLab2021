package commands.quiz

import Routes
import com.github.ajalt.clikt.output.TermUi.echo
import commands.Command
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.Answers
import model.AnswersResult
import model.QuestionsList
import java.util.*

class StartTest(private val httpClient: HttpClient,
                private val id: String,
                private val username: String) : Command {

    private val scanner = Scanner(System.`in`)

    override suspend fun execute(): Boolean {
        val testId = id.toIntOrNull() ?: throw IllegalArgumentException()
        try {
            val response = httpClient.get<HttpResponse>(Routes.getUrl(Routes.QUESTIONS) + testId)
            if (response.status != HttpStatusCode.OK) {
                echo("${response.receive<String>()}\n")
            }
            else {
                showTest(response.receive(), testId)
            }
        } catch (cause: ResponseException) {
            cause.response
        }
        return false
    }

    private suspend fun showTest(questionsList: QuestionsList, id: Int) {
        val answers = mutableListOf<Int>()
        questionsList.questionsList.forEach { question ->
            echo(question.getQuestion(), false)
            while (true) {
                val answer = scanner.nextLine()
                //TODO(variable number of answers)
                if (!validateAnswer(answer, 4))  {
                    echo("Incorrect answer format!!! Number in range 1..4 excepted.")
                } else {
                    answers.add(answer.toInt())
                    break
                }
            }
        }
        try {
            val response = httpClient.post<HttpResponse>(Routes.getUrl(Routes.RESULT)) {
                body = Answers(answers.toList(), id, username)
            }
            val result = response.receive<AnswersResult>()
            echo("Yours result: ${result.resultSum}\n")
        } catch (cause: ResponseException) {
            cause.response
        }
    }

    private fun validateAnswer(answer: String, answersQuantity: Int): Boolean {
        if (answersQuantity < 0) throw IllegalArgumentException()
        if (answer.isNotBlank()) {
            val number = answer.toIntOrNull() ?: return false
            return number in 1..answersQuantity
        }
        return false
    }
}