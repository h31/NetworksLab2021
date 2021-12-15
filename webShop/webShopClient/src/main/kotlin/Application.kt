import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.statement.*
import io.ktor.http.*


suspend fun main() {
    //initializing the client with JSON parser for models deserialization
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
        HttpResponseValidator {
            handleResponseException { exception ->
                val clientException = exception as? ClientRequestException ?: return@handleResponseException
                val exceptionResponse = exception.response
                when (exceptionResponse.status) {
                    HttpStatusCode.BadRequest -> println(exceptionResponse.readText())
                    HttpStatusCode.NotFound -> println(exceptionResponse.readText())
                    else -> println("Error code:${exceptionResponse.status.value}\n" +
                            "Description:${exceptionResponse.status.description}")
                }
            }
        }
    }

    //Auth.kt
    //authorizing - as a result, we should get the token
    val token = authorize(client)

    //all the commands and their arguments
    printHelpMsg()

    //Shop.kt
    //main interaction loop
    interact(client, token)
}