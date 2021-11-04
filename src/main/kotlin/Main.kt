import java.lang.NumberFormatException

fun main(args: Array<String>) {
    when (args[0]) {
        "-s" -> {
            var port = PORT
            if (args.size == 2)
                try {
                    port = args[1].toInt()
                } catch (e: NumberFormatException) {
                    println("Incorrect port format. Used default port: $port")
                }
            val server = Server(port)
            server.run()
        }
        "-c" -> {
            val client = Client()
            client.run()
        }
    }
}
