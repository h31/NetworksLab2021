package com.monkeys.ntp

import com.monkeys.ntp.models.WorkType
import com.monkeys.ntp.models.WorkType.*

const val STANDARD_PORT = 8081

const val DOWNLOADS_DIR = "PCSS downloads/"

fun parseArguments(args: List<String>): WorkType = when {
    args.isEmpty() -> {
        SERVER
    }
    args.contains("-c") && args.size == 1 -> {
        CLIENT
    }
    args.contains("-c") && args.size > 1 -> {
        CLIENT_WITH_ARGUMENTS
    }
    args.contains("-help") -> {
        HELP
    }
    else -> {
        println("Были переданы неверные аргументы, сверитесь с 'help':")
        HELP
    }
}

fun printHelp() {
    println(
        "\nДефолтно запуск без аргументов - запуск сервера (на порте 8080)\n" +
                "-с - запуск клиента\n" +
                "-help - вывод help меню\n" +
                "При неверных аргументах тоже выводится хэлп\n"
    )
}


fun getFixedLengthString(dataSize: Int): String {
    return dataSize.toString().padStart(8, '0')
}



