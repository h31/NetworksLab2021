package com.monkeys.pcss.models.message

import org.intellij.lang.annotations.Language
import java.io.File

fun parseData(dataMessage: String): Data {
    @Language("RegExp")
    val fileSizeRegex = """[A-Za-z0-9]+"""
    @Language("RegExp")
    val senderNameRegex = "[A-Za-z0-9А-Яа-я]+"
    @Language("RegExp")
    val timeRegex = """[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}\.[0-9]+((\+[0-9]{2}:[0-9]{2})|Z)\{[A-Za-z/]+\}"""
    @Language("RegExp")
    val messageTextRegex = """[^\[\]]*"""
    @Language("RegExp")
    val fileNameRegex = """([^(\[\])]+)\.([a-z0-9A-Z]+)"""
    val fullRegex =
        """_\[($fileSizeRegex)?\],\[($senderNameRegex)\],\[($timeRegex)?\],\[($messageTextRegex)?\],\[($fileNameRegex)?\]_;_""".toRegex()
    val matchResult = fullRegex.matchEntire(dataMessage)
    return if (matchResult != null) {
        val messageId = matchResult.groupValues[1]
        val senderName = matchResult.groupValues[2]
        val time = matchResult.groupValues[3]
        val messageText = matchResult.groupValues[6]
        val fileName = matchResult.groupValues[7]
        Data(messageId.toInt(), senderName, time, messageText, fileName)
    } else {
        Data()
    }
}

fun parseHeader(headerMessage: String): Header {
    val headerRegex =
        """\[([0-2])\],\[([01])\],\[([0-9]+)\]_;""".toRegex()

    val matchResult = headerRegex.matchEntire(headerMessage)
    return if (matchResult != null) {
        val type = matchResult.groupValues[1]
        val isFileAttached = matchResult.groupValues[2]
        val dataSize = matchResult.groupValues[3]
        Header(MessageType.values()[type.toInt()], (isFileAttached == "1"), dataSize.toInt())
    } else {
        Header()
    }
}

fun parseMessage(message: String): Message? {
    return if (message.isNotEmpty() && message.isNotBlank()) {
        val splitMessage = message.split("_;_")
        val header = parseHeader(splitMessage[0])
        val data = parseData(splitMessage[1])
        Message(header, data)
    } else {
        null
    }
}

fun parseHostAndPort(arg: String): Pair<String, Int> {
    return try {
        val arg1 = arg.split("[", "]")[0]
        val arg2 = arg.split(":")
        Pair(arg1, arg2[arg2.size - 1].toInt())
    } catch (e: Exception) {
        e.printStackTrace()
        Pair("Error", 0)
    }
}

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
        Pair(
            msg.replace("[", "_%+<+$")
                .replace("]", "_%+>+\$"), null
        )
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
