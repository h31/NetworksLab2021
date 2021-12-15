import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.AuthData
import model.AuthSuccess

suspend fun authorize(client: HttpClient): String {
    var login: String
    var pwdHash: String
    var token: String

    while (true) {
        println("hello! Want to register or login?(r/l)")

        //choosing the mode
        val mode = readLine()!!
        if (mode != "r" && mode != "l") {
            println("Please, enter only one letter(r/l). Is it so hard?")
            continue
        }

        //getting the credentials to form AuthData
        println("Great! Now, please, provide your credentials.")
        println("login:")
        login = readLine()!!
        println("password:")
        pwdHash = readLine()!!.hashCode().toString(radix = 16)
        val authData = AuthData(login, pwdHash)

        //registration - if the login is already taken(BadRequest) - start the auth again
        //and after successful registration you still need to log in to grab the token
        if (mode == "r") {
            println("Great! trying to register you...")
            val response: HttpResponse = client.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                body = authData
            }
            println(response.readText())
            if (response.status == HttpStatusCode.BadRequest) { continue }
        }

        //mode == "l"
        //ClientRequestException shows if there is no such login/pwdHash combination - starting the process again
        else {
            println("Great! trying to log you in...")
            try {
                val response = client.post<AuthSuccess>("$baseUrl/auth/login") {
                    contentType(ContentType.Application.Json)
                    body = AuthData(login, pwdHash)
                }

                //if everything is nice - we have the token
                token = response.jwtToken
                println("Great! you are logged in.")
                return token
            } catch (e: ClientRequestException) {
                println(e.localizedMessage)
                continue
            }
        }
    }
}