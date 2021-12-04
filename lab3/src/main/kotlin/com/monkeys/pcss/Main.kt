package com.monkeys.pcss

import com.monkeys.pcss.client.Client
import com.monkeys.pcss.models.WorkType.*
import com.monkeys.pcss.models.message.parseHostAndPort
import com.monkeys.pcss.server.Server
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private val logger: Logger = LoggerFactory.getLogger("com.monkeys.pcss.MainKt")

fun main(args: Array<String>) {
    when (parseArguments(args.toList())) {
        SERVER -> {
            val server = Server("0.0.0.0", 8081)
            server.start()
        }

        SERVER_WITH_ARGUMENTS -> {
            val serverArgumentIndex = args.indexOf("-s") + 1
            val arg = parseHostAndPort(args[serverArgumentIndex])
            if (arg.first == "Error") {
                logger.error("Incorrect arguments.\n" +
                        "Connection not establishment")
                printHelp()
            } else {
                try {
                    val server = Server(arg.first, arg.second)
                    server.start()
                } catch (e: Exception) {
                    logger.error(
                        "Incorrect arguments or it is impossible to establish a connection with the specified server.\n" +
                                "Connection not establishment"
                    )
                }
            }
        }

        CLIENT -> {
            val client = Client()
            runBlocking { client.start("localhost", 8081) }
        }

        CLIENT_WITH_ARGUMENTS -> {
            if (args.indexOf("-c") == 1)
                throw Exception()
            val clientArgumentIndex = args.indexOf("-c") + 1
            val arg = parseHostAndPort(args[clientArgumentIndex])
            if (arg.first == "Error") {
                logger.error(
                    "Incorrect arguments.\n" +
                            "Connection not establishment"
                )
                printHelp()
            } else {
                try {
                    val client = Client()
                    runBlocking { client.start(arg.first, arg.second) }
                } catch (e: Exception) {
                    logger.error(
                        "Incorrect arguments or it is impossible to establish a connection with the specified server.\n" +
                                "Connection not establishment"
                    )
                }
            }
        }

        HELP -> {
            printHelp()
        }
    }
}