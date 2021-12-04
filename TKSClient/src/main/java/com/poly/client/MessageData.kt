package com.poly.client

import com.poly.client.util.FILE_POINT
import com.poly.client.util.VOID
import com.poly.models.Message
import com.poly.models.MessageWithContent
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

object MessageData {
    var userName = VOID

    private class PolyFile(val fileName: String? = null, val fileSize: Int? = null, val fileContent: ByteArray? = null)

    private fun getPolyFile(path: String): PolyFile {
        val fileName = File(path).name
        val fileContent = Files.readAllBytes(Paths.get(path))
        val fileSize = fileContent.size
        return PolyFile(fileName, fileSize, fileContent)
    }

    fun createMessage(message: String): MessageWithContent {
        val partsOfMessage = message.split(FILE_POINT).toMutableList()
        var polyFile = PolyFile()
        if (partsOfMessage.size > 1) {
            if (!File(partsOfMessage[1].trim()).exists() || File(partsOfMessage[1].trim()).isDirectory) {
                partsOfMessage[0] += "fp:-/${partsOfMessage[1]}"
            } else polyFile = getPolyFile(partsOfMessage[1].trim())
        }
        return MessageWithContent(
            Message(null, userName, partsOfMessage[0], polyFile.fileName, polyFile.fileSize),
            polyFile.fileContent
        )
    }
}
