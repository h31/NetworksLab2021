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

}