package com.monkeys.ntp

import com.monkeys.ntp.client.Client
import com.monkeys.ntp.models.WorkType.*
import com.monkeys.ntp.models.parseHostAndPort
import com.monkeys.ntp.server.Server
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    when(parseArguments(args.toList())) {
        SERVER -> {
            val server = Server()
            server.start()
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
                println("Incorrect arguments or it is impossible to establish a connection with the specified server.\n" +
                        "Connection not establishment")
            }
        }

        HELP -> {
            printHelp()
        }
    }
}