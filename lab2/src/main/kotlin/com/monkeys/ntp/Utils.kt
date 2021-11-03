package com.monkeys.ntp

import com.monkeys.ntp.models.WorkType
import com.monkeys.ntp.models.WorkType.*

const val NTP_PORT = 123
const val NTP_PACKET_SIZE = 48
const val NTP_MODE_CLIENT = 3
const val NTP_MODE_SERVER = 4
const val NTP_VERSION = 4
const val ROOT_DELAY_OFFSET = 4
const val ROOT_DISPERSION_OFFSET = 8
const val REF_ID_OFFSET = 12
const val REFERENCE_OFFSET = 16
const val ORIGINATE_OFFSET = 24
const val RECEIVE_OFFSET = 32
const val TRANSMIT_OFFSET = 40

const val NTP_LEAP_NO_SYNC = 3
const val NTP_STRATUM_UNACCEPTABLE: Byte = 0
const val NTP_STRATUM_MAX = 15

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
        buffer[i] = (seconds and 0xff).toByte()
        seconds = seconds shr 8
    }

    var fraction = milliseconds * 0x100000000L / 1000L

    for (i in 7 downTo 4) {
        buffer[i] = (fraction and 0xff).toByte()
        fraction = fraction shr 8
    }
}

fun checkValidServerResponse(leapIndicator: Int,versionNumber: Int, mode: Int, stratum: Byte) {
    if (leapIndicator == NTP_LEAP_NO_SYNC)
        throw IllegalStateException("Server isn't synchronized")
    else if (versionNumber != NTP_VERSION)
        throw IllegalStateException("Untrusted version")
    else if (mode != NTP_MODE_SERVER)
        throw IllegalStateException("Untrusted mode")
    else if (stratum == NTP_STRATUM_UNACCEPTABLE || stratum > NTP_STRATUM_MAX)
        throw IllegalStateException("Untrusted mode")
}

fun Long.toByteArray() : ByteArray {
    val bytes = ByteArray(4)
    bytes[3] = (this and 0xFFFF).toByte()
    bytes[2] = ((this ushr 8) and 0xFFFF).toByte()
    bytes[1] = ((this ushr 16) and 0xFFFF).toByte()
    bytes[0] = ((this ushr 24) and 0xFFFF).toByte()
    return bytes
}

fun parseHostAndPort(arg: String) : Pair<String, Int> {
    return try {
        Pair(arg.split(":")[0], arg.split(":")[1].toInt())
    } catch (e: Exception) {
        Pair("127.0.0.1", 8081)
    }
}


