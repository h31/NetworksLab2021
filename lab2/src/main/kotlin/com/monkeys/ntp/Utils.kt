package com.monkeys.ntp

import com.monkeys.ntp.models.WorkType
import com.monkeys.ntp.models.WorkType.*

const val DOWNLOADS_DIR = "PCSS downloads/"

const val NTP_PORT = 123
const val NTP_PACKET_SIZE = 48
const val NTP_MODE_CLIENT = 3
const val NTP_MODE_SERVER = 4
const val NTP_VERSION = 3
const val TRANSMIT_OFFSET = 40

//70 years + 17 days (1904, 1908, ..., 1968) to sec
const val OFFSET_1900_TO_1970 = (365L * 70L + 17L) * 24L * 60L * 60L

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


fun writeTimeStamp(buffer: ByteArray, time: Long) {
    var offset = 0
    var seconds = time / 1000L
    val milliseconds = time % 1000L

    seconds += OFFSET_1900_TO_1970

    buffer[offset++] = (seconds shr 24).toByte()
    buffer[offset++] = (seconds shr 16).toByte()
    buffer[offset++] = (seconds shr 8).toByte()
    buffer[offset++] = seconds.toByte()

    val fraction = milliseconds * 0x100000000L / 1000L

    buffer[offset++] = (fraction shr 24).toByte()
    buffer[offset++] = (fraction shr 16).toByte()
    buffer[offset++] = (fraction shr 8).toByte()
    buffer[offset] = fraction.toByte()
}



