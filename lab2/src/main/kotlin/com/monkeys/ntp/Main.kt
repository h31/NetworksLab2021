package com.monkeys.ntp

import com.monkeys.ntp.client.Client
import com.monkeys.ntp.models.WorkType.*
import com.monkeys.ntp.server.Server
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
    when(parseArguments(args.toList())) {
        SERVER -> {
            val server = Server()
            server.start()
        }

        CLIENT -> {
            val client = Client("pool.ntp.org", NTP_PORT)
            runBlocking { client.start() }
        }

        CLIENT_WITH_ARGUMENTS -> {
            val clientArgumentIndex = args.indexOf("-c") + 1
            val arg = parseHostAndPort(args[clientArgumentIndex])
            val client = Client(arg.first, arg.second)
            runBlocking { client.start() }
        }

        HELP -> {
            printHelp()
        }
    }
}