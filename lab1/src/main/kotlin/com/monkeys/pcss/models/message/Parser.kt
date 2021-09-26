package com.monkeys.pcss.models.message

import java.io.File
import java.util.*

fun parseData(dataMessage: String): Data {
    val regex =
        """\[([A-Za-z0-9]+)?\],\[([A-Za-z0-9А-Яа-я]+)\],\[((([0,1][0-9])|(2[0-3])):[0-5][0-9])?\],\[([^\[\]]*)\],\[(([^(\[\])]+)\.([a-z0-9A-Z]+))?\]""".toRegex()
    val matchResult = regex.matchEntire(dataMessage)
    return if (matchResult != null) {
        val messageId = matchResult.groupValues[1]
        val senderName = matchResult.groupValues[2]
        val time = matchResult.groupValues[3]
        val messageText = matchResult.groupValues[7]
        val fileName = matchResult.groupValues[8]
        Data(messageId, senderName, time, messageText, fileName)
    } else {
        Data()
    }
}

fun parseHeader(headerMessage: String): Header {
    val regex =
        """\[([0-2])\],\[([01])\],\[([0-9]+)\]""".toRegex()

    val matchResult = regex.matchEntire(headerMessage)
    return if (matchResult != null) {
        val type = matchResult.groupValues[1]
        val isFileAttached = matchResult.groupValues[2]
        val dataSize = matchResult.groupValues[3]
        Header(MessageType.values()[type.toInt()], (isFileAttached == "1"), dataSize.toInt())
    } else {
        Header()
    }
}

fun parseMessage(message: String) : Message {
    val splitMessage = message.split("_;_")
    val header = parseHeader(splitMessage[0])
    val data = parseData(splitMessage[1])
    val file = if (splitMessage.size > 2) Base64.getDecoder().decode(splitMessage[2]) else ByteArray(0)
    return Message(header, data, file)
}

fun parseHostAndPort(arg: String) : Pair<String, Int> =
    Pair(arg.split(":")[0], arg.split(":")[1].toInt())

fun parseUserMessage(msg: String) : Pair<String,File?> {
    val splitMsg = msg.split("[[")
    var filePath = splitMsg[splitMsg.size - 1]
    filePath = filePath.filterNot { str -> "]]".contains(str) }
    val file = File(filePath)
    return if (file.isFile) {
        when (splitMsg.size) {
            1 -> Pair(msg, null)
            2 -> Pair(splitMsg[0], file)
            else -> Pair(collectMessage(splitMsg), file)
        }
    } else {
        Pair(msg, null)
    }

}

fun collectMessage(splitMsg: List<String>): String {
    val builder = StringBuilder()
    for (i in 0..splitMsg.size-2) {
        builder.append(splitMsg[i])
        builder.append("[[")
    }
    return builder.toString().substring(0, builder.length-2)
}