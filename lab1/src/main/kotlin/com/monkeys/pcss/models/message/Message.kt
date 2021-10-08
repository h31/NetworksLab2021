package com.monkeys.pcss.models.message

data class Message(
    var header: Header = Header(),
    var data: Data = Data()
) {
    constructor(message: String) : this() {
        val parsedMessage = parseMessage(message)
        header = parsedMessage!!.header
        data = parsedMessage.data
    }

    fun getMessage() =
        "${header.getHeader()}${data.getServerMessage()}".toByteArray()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (header != other.header) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = header.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }

}