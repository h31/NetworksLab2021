import java.lang.NumberFormatException

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        println("Incorrect command format. No arguments found")
        return
    }
    when (args[0]) {
        "-s" -> {
            if (args.size > 2) {
                println("Incorrect command format. Too many arguments")
                return
            }
            var port = PORT
            if (args.size == 2)
                try {
                    port = args[1].toInt()
                } catch (e: NumberFormatException) {
                    println("Incorrect port format.")
                    return
                }
            val server = Server(port)
            server.run()
        }
        "-c" -> {
            if (args.size > 1) {
                println("Incorrect command format. Too many arguments")
                return
            }
            val client = Client()
            client.run()
        }
        else -> {
            println("Incorrect command format. Example: \"-s <port>\" or \"-c\"")
        }
    }
}
