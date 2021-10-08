package com.monkeys.pcss.models.message

data class Header(
    //standard size = 20 byte

    var type: MessageType = MessageType.LOGIN,
    var isFileAttached: Boolean = false,
    var dataSize: Int = 0
) {
    constructor(headerMessage: String) : this() {
        val header = parseHeader(headerMessage)
        type = header.type
        isFileAttached = header.isFileAttached
        dataSize = header.dataSize
    }

    fun getHeader() =
        "[${type.ordinal}],[${if (isFileAttached) 1 else 0}],[${com.monkeys.pcss.getFixedLengthString(dataSize)}]_;"
}