import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*



suspend fun main() {
    //initializing the client with JSON parser for models deserialization
    val client = HttpClient(CIO) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
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