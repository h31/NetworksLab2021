package com.monkeys.pcss.client

import com.monkeys.pcss.*
import com.monkeys.pcss.models.message.*
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
            "" -> {
                stillWorking = false
            }
            null -> {
                stillWorking = false
            }
            "q" -> {
                sendMessage(sender, "EXIT".toByteArray(), null)
                stillWorking = false
            }
            else -> {

                val data = Data(0, userInput, "", "", null)
                val dataSize = data.getServerMessage().length
                val header = Header(MessageType.LOGIN, false, dataSize)
                val message = Message(header, data)

                sender.write(message.getMessage())
                sender.flush()

                var messageInfo = ""

                while (isSingingInNow) {
                    if (receiver.available() > 0) {

                        val fullServerMessage = getNewMessage(receiver)
                        val serverMessage = fullServerMessage.first
                        messageInfo = serverMessage!!.data.messageText
                        val type = serverMessage.header.type
                        val senderName = serverMessage.data.senderName

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
                if (messageInfo != "Name is taken, please try to connect again")
                    println("You can attach a picture by writing such a construction at the end of the message [[filepath]]")
            }
        }
        if (nameExist) {
            stopConnection()
        } else {
            launch(Dispatchers.IO) { sendMessages() }
            launch(Dispatchers.IO) { receiveMessages() }
        }
    }

    private fun sendMessages() {
        try {
            while (stillWorking) {
                print("m: ")
                when (val userMessage = readLine()) {
                    "" -> continue
                    "q" -> {
                        sendMessage(sender, "EXIT".toByteArray(), null)
                        stillWorking = false
                    }
                    else -> {
                        val parsedMessage = parseUserMessage(userMessage.toString())
                        val msg = parsedMessage.first
                        val file = parsedMessage.second
                        var fileName = file?.name
                        var fileByteArray = ByteArray(0)

                        if (file != null) {
                            val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
                            val fileType = fileNameMap.getContentTypeFor(fileName).split("/")[0]

                            if (!setOf("image", "video", "audio").contains(fileType)) {
                                fileName = ""
                                println("You can only attach media files, any others may be unsafe. Your file was not attached")
                            } else {
                                if (file.canRead()) {
                                    fileByteArray = file.readBytes()
                                } else {
                                    println("Can't read file, sending message without it")
                                }
                            }
                        }

                        val data = Data(fileByteArray.size, name, "", msg, fileName)
                        val dataSize = data.getServerMessage().length
                        val header = Header(MessageType.MESSAGE, fileByteArray.isNotEmpty(), dataSize)
                        val message = Message(header, data).getMessage()

                        if (header.isFileAttached) {
                            sender.write(message.plus(fileByteArray))
                            sender.flush()
                        } else {
                            sender.write(message)
                            sender.flush()
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

    private fun receiveMessages() {
        try {
            while (stillWorking) {
                if (receiver.available() > 0) {

                    val fullMessage = getNewMessage(receiver)
                    val serverMessage = fullMessage.first
                    val fileByteArray = fullMessage.second

                    val messageType = serverMessage.header.type
                    val serverData = serverMessage.data

                    if (messageType == MessageType.MESSAGE) {

                        val serverZoneDateTime = serverData.time.replace("{", "[").replace("}", "]")
                        val id = TimeZone.getDefault().id
                        val parsedSZDT = ZonedDateTime.parse(serverZoneDateTime)
                        val clientSZDT = parsedSZDT.withZoneSameInstant(ZoneId.of(id))
                            .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM))

                        var finalData = Data(
                            serverData.fileSize, serverData.senderName,
                            clientSZDT, serverData.messageText, serverData.fileName
                        )

                        if (serverMessage.header.isFileAttached) {
                            val fileName = finalData.fileName
                            val senderName = finalData.senderName
                            val time = finalData.time
                            val finalFileName = shapeFileName(fileName!!, senderName, time)
                            val file1 = File(DOWNLOADS_DIR + finalFileName)
                            file1.createNewFile()
                            file1.writeBytes(fileByteArray)
                            finalData = Data(
                                serverData.fileSize, serverData.senderName,
                                clientSZDT, serverData.messageText, finalFileName
                            )

                            val fileNameMap: FileNameMap = URLConnection.getFileNameMap()
                            val fileType = fileNameMap.getContentTypeFor(fileName).split("/")[0]
                            if (fileType == "image") {
                                println(finalData.getClientMessage(File(file1.absolutePath)))
                                print("m: ")
                            } else {
                                println(finalData.getClientMessage(null))
                                print("m: ")
                            }

                        } else {
                            println(finalData.getClientMessage(null))
                            print("m: ")
                        }
                    } else {
                        println(serverData.messageText)
                        print("m: ")
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