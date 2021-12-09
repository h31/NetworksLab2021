package com.monkeys.pcss.client

import com.monkeys.pcss.*
import com.monkeys.pcss.models.message.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.io.OutputStream
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

class Client(private val host: String, private val port: Int) {

    private lateinit var name: String
    private lateinit var socket: Socket
    private lateinit var receiver: ByteReadChannel
    private lateinit var sender: ByteWriteChannel
    private val logger = LoggerFactory.getLogger("com.monkeys.pcss.client.ClientKt")

    private suspend fun readLineSuspending(): String =
        withContext(Dispatchers.IO) { return@withContext readLine()!! }

    suspend fun start() = coroutineScope {
        socket = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp().connect(InetSocketAddress(host, port))
        receiver = socket.openReadChannel()
        sender = socket.openWriteChannel(true)

        var nameExist = false
        println("Enter your nickname or \'q\' to exit.")

        when (val userInput = readLineSuspending()) {
            "" -> {
                stopConnection()
            }
            "q" -> {
                sender.writeFully(ByteBuffer.wrap("EXIT".toByteArray()))
                stopConnection()
            }
            else -> {

                val data = Data(0, userInput, "", "", null)
                val dataSize = data.getServerMessage().toByteArray().size
                val header = Header(MessageType.LOGIN, false, dataSize)
                val message = ByteBuffer.wrap(Message(header, data).getMessage())

                sender.writeFully(message)

                var messageInfo = ""

                val fullServerMessage = getNewMessage(receiver)
                val serverMessage = fullServerMessage.first
                messageInfo = serverMessage.data.messageText
                val type = serverMessage.header.type
                val senderName = serverMessage.data.senderName

                if (messageInfo == "Name is taken, please try to connect again"
                    && type == MessageType.LOGIN && senderName == "server"
                ) {
                    //
                    nameExist = true
                } else {
                    name = userInput
                    nameExist = false
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

    private suspend fun sendMessages() {
        try {
            while (!socket.isClosed) {
                print("m: ")
                when (val userMessage = readLine()) {
                    "" -> continue
                    "q" -> {
                        sender.writeFully(ByteBuffer.wrap("EXIT".toByteArray()))
                        stopConnection()
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
                                logger.error("You can only attach media files, any others may be unsafe. Your file was not attached")
                            } else {
                                if (file.canRead()) {
                                    fileByteArray = file.readBytes()
                                } else {
                                    logger.error("Can't read file, sending message without it")
                                }
                            }
                        }

                        val data = Data(fileByteArray.size, name, "", msg, fileName)
                        val dataSize = data.getServerMessage().toByteArray().size
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
            logger.error("!E: There is an ERROR while sending ur message. Probably the server was destroyed by evil goblins.")
            e.printStackTrace()
            stopConnection()
        }
    }

    private suspend fun receiveMessages() {
        try {
            while (!socket.isClosed) {

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
        } catch (e: Exception) {
            logger.error("!E: There is an ERROR while receiving new messages. Probably the server was destroyed by evil goblins.")
            e.printStackTrace()
            stopConnection()
        }
    }


    private fun stopConnection() {
        try {
            socket.close()
            println("Bye!")
        } catch (e: SocketException) {
            logger.error("ERROR! Socket wasn't closed by client(probably it was closed by server)!")
        }
    }
}