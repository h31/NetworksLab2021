package com.monkeys.pcss.models.message

import java.util.*

data class Message(
    var header: Header = Header(),
    var data: Data = Data(),
    var file: ByteArray = ByteArray(0)
) {
    constructor(message: String) : this() {
        val parsedMessage = parseMessage(message)
        header = parsedMessage.header
        data = parsedMessage.data
        file = parsedMessage.file
    }

    fun getMessage() =
        "${header.getHeader()}${data.getServerMessage()}" + Base64.getEncoder().encodeToString(file) + "\n"


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (header != other.header) return false
        if (data != other.data) return false
        if (!file.contentEquals(other.file)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + file.contentHashCode()
        return result
    }

}