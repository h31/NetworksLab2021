package com.monkeys.pcss.client

import com.monkeys.pcss.*
import com.monkeys.pcss.models.message.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.FileNameMap
import java.net.InetSocketAddress
import java.net.SocketException
import java.net.URLConnection
import java.nio.ByteBuffer
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class Client(host_: String, port_: Int) {

    private val host = host_
    private val port = port_
    private lateinit var name: String
    private var stillWorking = true

    suspend fun start() = coroutineScope {
        val socket = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp().connect(InetSocketAddress(host, port))
        val receiver = socket.openReadChannel()
        val sender = socket.openWriteChannel(true)

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
                sender.writeFully(ByteBuffer.wrap("EXIT".toByteArray()))
                stillWorking = false
            }
            else -> {

                val data = Data(0, userInput, "", "", null)
                val dataSize = data.getServerMessage().length
                val header = Header(MessageType.LOGIN, false, dataSize)
                val message = ByteBuffer.wrap(Message(header, data).getMessage())

                sender.writeFully(message)
                sender.flush()

                var messageInfo = ""

                while (isSingingInNow) {
                    if (receiver.availableForRead > 0) {

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
            stopConnection(socket, sender, receiver)
        } else {
            launch(Dispatchers.IO) { sendMessages(socket, sender, receiver) }
            launch(Dispatchers.IO) { receiveMessages(socket, sender, receiver) }
        }
    }

    private suspend fun sendMessages(socket: Socket, sender: ByteWriteChannel, receiver: ByteReadChannel) {
        try {
            while (stillWorking) {
                print("m: ")
                when (val userMessage = readLine()) {
                    "" -> continue
                    "q" -> {
                        sender.writeFully(ByteBuffer.wrap("EXIT".toByteArray()))
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
                        val f = message.plus(fileByteArray)

                        if (header.isFileAttached) {
                            sender.writeFully(f)
                        } else {
                            sender.writeFully(ByteBuffer.wrap(message))
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("!E: There is an ERROR while sending ur message. Probably the server was destroyed by evil goblins.")
            e.printStackTrace()
            stopConnection(socket, sender, receiver)
        }
    }

    private suspend fun receiveMessages(socket: Socket, sender: ByteWriteChannel, receiver: ByteReadChannel) {
        try {
            while (stillWorking) {
                if (receiver.availableForRead > 0) {

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
            stopConnection(socket, sender, receiver)
        }
    }


    private fun stopConnection(socket: Socket, sender: ByteWriteChannel, receiver: ByteReadChannel) {
        try {
            //receiver.close()
            sender.close()
            socket.close()
            println("Bye!")
        } catch (e: SocketException) {
            println("ERROR! Socket wasn't closed by client(probably it was closed by server)!")
        }
    }
}