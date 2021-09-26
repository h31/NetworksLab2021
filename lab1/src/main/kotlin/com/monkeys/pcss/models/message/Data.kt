package com.monkeys.pcss.models.message

import com.monkeys.pcss.kit

data class Data(
    var messageId: String? = null,
    var senderName: String = "",
    var time: String = "",
    var messageText: String = "",
    var fileName: String? = null
) {


    constructor(dataMessage: String) : this() {
        val data = parseData(dataMessage)
        messageId = data.messageId
        senderName = data.senderName
        time = data.time
        messageText = data.messageText.replace("[", "_%+<+$")
            .replace("]", "_%+>+\$")
        fileName = data.fileName
    }

    fun getClientMessage(): String =
        "<$time> [$senderName] :: ${messageText.replace( "_%+<+$","[")
            .replace("_%+>+\$", "]")} :: " +
                "(${if (fileName.isNullOrEmpty()) "no file" else fileName} attached)\n" +
                if (!fileName.isNullOrEmpty()) {
                    //TODO() если фото, то
                    //берем файл, преобразуем его в аскии и вставляем
                    //иначе пофиг
                    "$fileName\n${kit()}"
                } else {
                    kit()
                }

    fun getServerMessage(): String =
        "_[${messageId ?: ""}],[$senderName],[$time],[$messageText],[${fileName?: ""}]_;_"
}