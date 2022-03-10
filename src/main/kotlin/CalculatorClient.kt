import com.google.gson.Gson
import com.google.gson.JsonObject
import data.Credentials
import data.Message
import data.User
import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.system.exitProcess


const val SERVER_URL = "http://localhost:8080"

val SIMPLE_OPERATION = listOf("plus", "minus", "mult", "div")
val COMPLEX_OPERATION = listOf("fact", "sqrt")

class CalculatorClient {
    private var token: String = ""
    private val client = HttpClient {
        install(JsonFeature) {
            serializer = GsonSerializer()
        }
        expectSuccess = false
    }

    fun start() {
        runBlocking {
            while (true) {
                println("Please, chose and write" + "\n" + "registration | login | exit")
                when (readLine()) {
                    "login" -> {
                        login()
                    }
                    "registration" -> {
                        registration()
                    }
                    "exit" -> {
                        println("Exit...")
                        exitProcess(0)
                    }
                    else -> {
                        println("Wrong format input")
                    }
                }
            }
        }
    }

    private suspend fun registration() {
        println("Please, write login")
        val login = readLine().toString()
        println("Please, write password")
        val password = readLine().toString()

        val response: HttpResponse = client.post("$SERVER_URL/auth") {
            contentType(ContentType.Application.Json)
            body = Credentials(login, password)
        }
        if (response.status.value == 200)
            println(
                "User registration with login ${
                    Gson().fromJson(
                        response.readText(),
                        Credentials::class.java)
                        .login
                } was successful"
            )
        else if (response.status.value == 403)
            println(
                Gson().fromJson(
                    response.readText(),
                    Message::class.java)
                    .message
            )
    }

    private suspend fun login() {
        println("Please, write login")
        val login = readLine().toString()
        println("Please, write password")
        val password = readLine().toString()

        val response: HttpResponse = client.post("$SERVER_URL/login") {
            contentType(ContentType.Application.Json)
            body = Credentials(login, password)
        }
        if (response.status.value == 200) {
            val user = Gson().fromJson(
                response.readText(),
                JsonObject::class.java)
                .getAsJsonObject("user")
            token = "bearer " + Gson().fromJson(
                response.readText(),
                JsonObject::class.java)
                .get("token").toString()

            println("User ${user.get("login")} authorization with access level ${user.get("access_level")}")
            calculator()
        } else if (response.status.value == 403)
            println(
                Gson().fromJson(
                    response.readText(),
                    Message::class.java)
                    .message
            )
    }

    private suspend fun logout() {
        println("Please, write login")
        val login = readLine().toString()

        val response: HttpResponse = client.post("$SERVER_URL/logout") {
            contentType(ContentType.Application.Json)
            body = Credentials(login, null)
        }
        if (response.status.value == 200) {
            val user = Gson().fromJson(
                response.readText(),
                User::class.java
            )
            token = ""
            println("User ${user.login} logout with access level ${user.access_level} successful")
        } else if (response.status.value == 403) {
            println(
                Gson().fromJson(
                    response.readText(),
                    Message::class.java)
                    .message
            )
        }
    }

    private fun calculator() {
        runBlocking {
            while (true) {
                println("Please, chose and write" + "\n" + "calculate | logout | exit")
                when (readLine()) {
                    "calculate" -> {
                        calculate()
                    }
                    "logout" -> {
                        logout()
                        break
                    }
                    "exit" -> {
                        println("Exit...")
                        exitProcess(0)
                    }
                    else -> {
                        println("Wrong format input")
                    }
                }
            }
        }
    }

    private suspend fun calculate() {
        val operation = StringBuilder().append("/")
        println("Please, chose and write" + "\n" + (SIMPLE_OPERATION + COMPLEX_OPERATION).joinToString(separator = " | "))
        readLine().let {
            when (it) {
                in SIMPLE_OPERATION + COMPLEX_OPERATION -> {
                    operation.append(it)
                }
                else -> {
                    println("Wrong format input")
                    return
                }
            }
        }
        try {
            println("Please, write first value")
            val o1 = readLine()?.toDouble() ?: throw NumberFormatException()

            if (SIMPLE_OPERATION.contains(operation.substring(1))) {
                println("Please, write second value")
                val o2 = readLine()?.toDouble() ?: throw NumberFormatException()
                operation.append("?o1=$o1&o2=$o2")
            } else
                operation.append("?o1=$o1")
        } catch (e: NumberFormatException) {
            println("Wrong format input")
            return
        }
        val response: HttpResponse = client.get(SERVER_URL + operation) {
            header("Authorization", token)
        }
        if (response.status.value == 200) {
            println("Result: " + Gson().fromJson(
                response.readText(),
                JsonObject::class.java)
                .get("result").toString()
            )
        } else if (response.status.value == 401) {
            println(
                Gson().fromJson(
                    response.readText(),
                    Message::class.java)
                    .message
            )
        }
    }
}