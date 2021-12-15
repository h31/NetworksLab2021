import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import model.Item

suspend fun interact(client: HttpClient, token: String) {
    while (true) {
        val input = readLine()!!.split(" ")
        if (!checkArgs(input)) continue

        when (input[0]) {
            "list" -> {
                val request = client.get<List<Item>>("$baseUrl/item"){
                    header(HttpHeaders.Authorization, "bearer $token")
                    contentType(ContentType.Application.Json)
                }
                for (item in request) {
                    println("${item.name} : ${item.amount} available for ${item.price} each.")
                }
            }
            "addPos" -> {
                try {
                    val name = input[1]
                    val amount = input[2].toInt()
                    val price = input[3].toDouble()
                    val request = client.post<String>("$baseUrl/item") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        contentType(ContentType.Application.Json)
                        body = Item(name, amount, price)
                    }
                    println(request)
                }
                catch (e: NumberFormatException) {
                    printArgFormatMsg()
                    continue
                }
            }
            "buy" -> {
                try {
                    val name = input[1]
                    val amount = input[2].toInt()
                    val request = client.put<String>("$baseUrl/item/buy") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        body = FormDataContent(Parameters.build {
                            append("name", name)
                            append("amount", amount.toString())
                        })
                    }
                    println(request)
                }
                catch (e: NumberFormatException) {
                    printArgFormatMsg()
                    continue
                }
                catch (e: ClientRequestException) {
                    println(e.message.split("Text: ")[1])
                    continue
                }
            }
            "add" -> {
                try {
                    val name = input[1]
                    val amount = input[2].toInt()
                    val request = client.put<String>("$baseUrl/item/add") {
                        header(HttpHeaders.Authorization, "Bearer $token")
                        body = FormDataContent(Parameters.build {
                            append("name", name)
                            append("amount", amount.toString())
                        })
                    }
                    println(request)
                }
                catch (e: NumberFormatException) {
                    printArgFormatMsg()
                    continue
                }
                catch (e: ClientRequestException) {
                    println(e.message.split("Text: ")[1])
                    continue
                }
            }
            "help" -> { printHelpMsg() }
            "exit" -> {
                println("Bye-bye!")
                break
            }
            else -> {
                println("There is no such command, try again.")
            }
        }
    }
}

fun checkArgs(args: List<String>): Boolean {
    val commands = mapOf("list" to 1, "addPos" to 4, "buy" to 3, "add" to 3, "exit" to 1, "help" to 1)
    if (!commands.keys.contains(args[0])) {
        printCommandNotFoundMsg()
        return false
    }
    else if (commands[args[0]] != args.size) {
        printArgAmountMsg()
        return false
    }
    return true
}

fun printArgAmountMsg() {
    println("Please, provide the correct amount of arguments.")
}

fun printArgFormatMsg() {
    println("Please, provide the correct arguments.")
}

fun printCommandNotFoundMsg() {
    println("there is no such command.")
}