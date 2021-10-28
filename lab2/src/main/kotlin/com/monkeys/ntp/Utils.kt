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
    var seconds = time / 1000L
    val milliseconds = time % 1000L

    seconds += OFFSET_1900_TO_1970

    for (i in 3 downTo 0) {
        buffer[i] = (seconds and 0xff).toCustomByte()
        seconds = seconds shr 8
    }

    var fraction = milliseconds * 0x100000000L / 1000L

    for (i in 7 downTo 4) {
        buffer[i] = (fraction and 0xff).toCustomByte()
        fraction = fraction shr 8
    }
}

fun Int.toCustomByte() = if (this > 127) (128 - this).toByte() else this.toByte()

fun Long.toCustomByte() = this.toInt().toCustomByte()



