package com.monkeys.pcss

import com.monkeys.pcss.models.WorkType
import com.monkeys.pcss.models.WorkType.*
import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import io.ktor.utils.io.*

const val STANDARD_PORT = 8081
const val STANDARD_HEADER_SIZE = 20
const val DOWNLOADS_DIR = "PCSS downloads/"

fun parseArguments(args: List<String>): WorkType = when {
    args.contains("-s") -> {
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

fun kit(): String =
    "     (\\.-./) \n" +
            "   = (^ Y ^) = \n" +
            "     /`---`\\\n" +
            "    |U_____U|" +
            "\n"

suspend fun getNewMessage(readChannel: ByteReadChannel): Pair<Message, ByteArray> {
    val headerByteArray = ByteArray(STANDARD_HEADER_SIZE)
    readChannel.readFully(headerByteArray, 0, STANDARD_HEADER_SIZE)
    val sHeader = String(headerByteArray)
    val header = Header(sHeader)
    val dataByteArray = ByteArray(header.dataSize)
    readChannel.readFully(dataByteArray, 0, header.dataSize)
    val sData = String(dataByteArray)
    val data = Data(sData)
    val message = Message(header, data)
    if (header.isFileAttached) {
        val fileByteArray = ByteArray(data.fileSize)
        readChannel.readFully(fileByteArray, 0, data.fileSize)
        return Pair(message, fileByteArray)
    }
    return Pair(message, ByteArray(0))
}

fun shapeFileName(fileName: String, senderName: String, time: String): String {
    val builder = StringBuilder()
    val split = fileName.split(".")
    builder.append(split[0])
    builder.append("_")
    builder.append(senderName)
    builder.append("_")
    builder.append(time.replace(":", "-"))
    builder.append(".")
    builder.append(split[1])
    return builder.toString()
}

fun getFixedLengthString(dataSize: Int): String {
    return dataSize.toString().padStart(8, '0')
}



