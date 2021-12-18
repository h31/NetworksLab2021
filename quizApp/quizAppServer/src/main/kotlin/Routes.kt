import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import models.*
import org.ktorm.dsl.*

fun Application.registerRoutes() {
    routing {
        routedAPI()
        routedAuth()
    }
}

fun Route.routedAuth() {
    val auth = Auth() //to establish db connection once and use only statements
    route("/auth") {
        post("/login") {
            val authData = call.receive<AuthData>()
            if (auth.login(authData.login, authData.pwdHash)) {
                val jwtToken = JWT.create()
                    .withClaim("login", authData.login)
                    .withClaim("pwdHash", authData.pwdHash)
                    .withSubject("Authentication")
                    .sign(Algorithm.HMAC256("Here is the secret"))
                return@post call.respond(status = HttpStatusCode.OK, AuthSuccess(jwtToken))
            }
            else {
                return@post call.respond(status = HttpStatusCode.NotFound, "There is no such username with these login/password.")
            }
        }
        post("/register") {
            val authData = call.receive<AuthData>()
            if (auth.register(authData.login, authData.pwdHash)) {
                return@post call.respond(status = HttpStatusCode.Created,"Registration successful. Now you can log in using your credentials at auth/login.")
            }
            else {
                return@post call.respond(status = HttpStatusCode.BadRequest, "Something went wrong. Maybe, this username is already taken?")
            }

        }
    }
}

fun Route.routedAPI() {
    authenticate("auth-jws") {
        route("/tests") {
            get {
                val tests = mutableListOf<Test>()
                for (entry in connect().from(TestTable).select()) {
                    tests.add(getTestFromEntry(entry))
                }
                return@get call.respond(status = HttpStatusCode.OK, TestsList(tests))
            }
            get("{id}") {
                val reqId = call.parameters["id"]?.toInt() ?: return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.BadRequest
                )

                val query = connect()
                    .from(TestTable)
                    .select()
                    .where { (TestTable.id eq reqId) }

                if (query.totalRecords == 0) {
                    return@get call.respondText("No test with id: $reqId", status = HttpStatusCode.NotFound)
                }
                //there will be ONLY ONE entry
                for (entry in query) {
                    return@get call.respond(getTestFromEntry(entry))
                }
            }
            post("/sendAnswers"){
                //getting the user answers
                val answers = call.receive<Answers>()

                //getting the correct answers and their values
                val correctAnswers = mutableListOf<Int>()
                val values = mutableListOf<Int>()
                connect()
                    .from(TestTable)
                    .select()
                    .where { QuestionTable.testId eq answers.testId }
                    .orderBy(QuestionTable.id.asc())
                    .forEach {
                        correctAnswers.add(it.getInt("answer"))
                        values.add(it.getInt("value"))
                    }


                //check if incoming answers are ok
                if (correctAnswers.size != answers.answers.size) {
                    return@post call.respondText("Incorrect amount of answers", status = HttpStatusCode.BadRequest)
                }
                //comparing and calculating the sum of values
                var resultSum = 0
                for (i in 0 until answers.answers.size) {
                    if (correctAnswers[i] == answers.answers[i]) {
                        resultSum += values[i]
                    }
                }

                //updating the user stats
                val login = answers.username
                connect()
                    .update(UserTable) {
                        set(it.lastTestId, answers.testId)
                        set(it.lastResult, resultSum)
                        where { it.login eq login }
                    }
                return@post call.respond(status = HttpStatusCode.OK, AnswersResult(resultSum))
            }
        }

        route("/questions") {
            get("{testId}") {
                val testIdIn = call.parameters["testId"]?.toInt() ?: return@get call.respondText(
                    "Missing or malformed id",
                    status = HttpStatusCode.BadRequest
                )
                val query = connect()
                    .from(QuestionTable)
                    .select()
                    .where { QuestionTable.testId eq testIdIn }
                    .orderBy(QuestionTable.id.asc())
                val questions = mutableListOf<Question>()
                for (entry in query) {
                    questions.add(getQuestionFromEntry(entry))
                }
                return@get call.respond(status = HttpStatusCode.OK, QuestionsList(questions))
            }
        }

        route("/users") {
            get("{login}") {
                val reqLogin = call.parameters["login"] ?: return@get call.respondText(
                    "Missing or malformed login",
                    status = HttpStatusCode.BadRequest
                )

                val query = connect()
                    .from(UserTable)
                    .select()
                    .where { UserTable.login eq reqLogin }

                if (query.totalRecords == 0) {
                    return@get call.respondText("No user with login: $reqLogin", status = HttpStatusCode.NotFound)
                }
                //there will be ONLY ONE entry
                for (entry in query) {
                    return@get call.respond(getUserFromEntry(entry))
                }

            }
        }
    }
}