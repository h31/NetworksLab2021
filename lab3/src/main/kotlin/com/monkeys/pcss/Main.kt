package com.monkeys.pcss

import com.monkeys.pcss.client.Client
import com.monkeys.pcss.models.WorkType.*
import com.monkeys.pcss.models.message.parseHostAndPort
import com.monkeys.pcss.server.Server
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    when (parseArguments(args.toList())) {
        SERVER -> {
            val server = Server("localhost", 8081)
            server.start()

        }

        SERVER_WITH_ARGUMENTS -> {
            val serverArgumentIndex = args.indexOf("-s") + 1
            val arg = parseHostAndPort(args[serverArgumentIndex])
            try {
                val server = Server(arg.first, arg.second)
                server.start()
            } catch (e: Exception) {
                println(
                    "Incorrect arguments or it is impossible to establish a connection with the specified server.\n" +
                            "Connection not establishment"
                )
            }
        }

        CLIENT -> {
            val client = Client("localhost", 8081)
            runBlocking { client.start() }
        }

        CLIENT_WITH_ARGUMENTS -> {
            val clientArgumentIndex = args.indexOf("-c") + 1
            val arg = parseHostAndPort(args[clientArgumentIndex])
            try {
                val client = Client(arg.first, arg.second)
                runBlocking { client.start() }
            } catch (e: Exception) {
                println(
                    "Incorrect arguments or it is impossible to establish a connection with the specified server.\n" +
                            "Connection not establishment"
                )
            }
        }

        HELP -> {
            printHelp()
        }
    }
}