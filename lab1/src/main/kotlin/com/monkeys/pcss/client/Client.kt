package com.monkeys.pcss.client

import com.monkeys.pcss.models.message.*
import com.monkeys.pcss.readMessageFromInputStream
import com.monkeys.pcss.send
import com.monkeys.pcss.shapingFileName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.net.FileNameMap
import java.net.Socket
import java.net.SocketException
import java.net.URLConnection
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class Client(host: String, port: Int) {

    private var socket: Socket = Socket(host, port)
    private val receiver = BufferedInputStream(socket.getInputStream())
    private val sender = BufferedOutputStream(socket.getOutputStream())
    private lateinit var name: String
    private var stillWorking = true

    suspend fun start() = coroutineScope {

        var nameExist = false
        var isSingingInNow = true
        println("Enter your nickname or \'q\' to exit.")

        when (val userInput = readLine()) {
            null -> {
                stillWorking = false
            }
            "q" -> {
                send(sender, "EXIT".toByteArray())
                stillWorking = false
            }
            else -> {

                val data = Data(null, userInput, "", "", null)
                val header = Header(MessageType.LOGIN, false, 0)
                val message = Message(header, data)

                send(sender, message.getMessage())

                var messageInfo = ""

                while (isSingingInNow) {
                    if (receiver.available() > 0) {

                        val serverMessage = readMessageFromInputStream(receiver)
                        val parsedServerMessage = parseMessage(serverMessage)
                        messageInfo = parsedServerMessage!!.data.messageText
                        val type = parsedServerMessage.header.type
                        val senderName = parsedServerMessage.data.senderName
                        if (messageInfo == "Name is taken, please try to connect again"
                            && type == MessageType.LOGIN && senderName == "server"
                        ) {
                            stillWorking = false
                            nameExist = true
                        } else {
                            name = userInput
                            nameExist = false
                        }
                        isSingingInNow = false
                    }
                }
                println(messageInfo)

            }
        }
        if (nameExist) {
            stopConnection()
        } else {
            launch(Dispatchers.IO) { sendingMessages() }
            launch(Dispatchers.IO) { receivingMessages() }
        }
    }

    private fun sendingMessages() {
        println("You can attach a picture by writing such a construction at the end of the message [[filepath]]")
        try {
            while (stillWorking) {
                print("m: ")
                when (val userMessage = readLine()) {
                    "" -> continue
                    "q" -> {
                        send(sender, "EXIT".toByteArray())
                        stillWorking = false
                    }
                    else -> {
                        val parsedMessage = parseUserMessage(userMessage.toString())
                        val msg = parsedMessage.first
                        var file = parsedMessage.second
                        var fileName = file?.name
                        var fileByteArray = ByteArray(0)

                        if (file != null) {
                            val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
                            val fileType = fileNameMap.getContentTypeFor(fileName).split("/")[0]

                            if (!setOf("image", "video", "audio").contains(fileType)) {
                                fileName = ""
                                file = null
                                println("You can only attach media files, any others may be unsafe. Your file was not attached")
                            } else {
                                fileByteArray = file.readBytes()
                            }
                        }

                        val data = Data(null, name, "", msg, fileName)
                        val header = Header(
                            MessageType.MESSAGE, file != null,
                            fileByteArray.size
                        )
                        val message = Message(header, data)

                        send(sender, message.getMessage())

                        if (header.isFileAttached) {
                            send(sender, fileByteArray)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("!E: There is an ERROR while sending ur message. Probably the server was destroyed by evil goblins.")
            e.printStackTrace()
            stopConnection()
        }
    }

    private fun receivingMessages() {
        try {
            while (stillWorking) {
                if (receiver.available() > 0) {
                    val serverMessage = readMessageFromInputStream(receiver)
                    val parsedServerMessage = parseMessage(serverMessage)
                    if (parsedServerMessage != null) {
                        val serverData = parsedServerMessage.data

                        val serverZoneDateTime = serverData.time.replace("{", "[").replace("}", "]")
                        val id = TimeZone.getDefault().id
                        val parsedSZDT = ZonedDateTime.parse(serverZoneDateTime)
                        val clientSZDT = parsedSZDT.withZoneSameInstant(ZoneId.of(id))
                            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))

                        val finalData = Data(
                            serverData.messageId, serverData.senderName,
                            clientSZDT, serverData.messageText, serverData.fileName
                        )

                        println(finalData.getClientMessage())
                        print("m: ")

                        val size = parsedServerMessage.header.fileSize
                        val byteArray = ByteArray(size)
                        if (parsedServerMessage.header.isFileAttached) {
                            receiver.read(byteArray)
                            val fileName = finalData.fileName
                            val senderName = finalData.senderName
                            val time = finalData.time
                            val file1 = File(shapingFileName(fileName!!, senderName, time))
                            file1.createNewFile()
                            file1.writeBytes(byteArray)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("!E: There is an ERROR while receiving new messages. Probably the server was destroyed by evil goblins.")
            e.printStackTrace()
            stopConnection()
        }
    }


    private fun stopConnection() {
        try {
            receiver.close()
            sender.close()
            socket.close()
            println("Bye!")
        } catch (e: SocketException) {
            println("ERROR! Socket wasn't closed by client(probably it was closed by server)!")
        }
    }
}
