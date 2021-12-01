package com.monkeys.pcss.server

import com.monkeys.pcss.getNewMessage
import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import com.monkeys.pcss.models.message.MessageType
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress
import java.time.ZonedDateTime

class Server(private val host: String, private val port: Int) {

    private val clientList = ClientList()

    fun start() = runBlocking {
        val server = aSocket(ActorSelectorManager(Dispatchers.IO))
            .tcp()
            .bind(InetSocketAddress(host, port))
        println("Server started")

        while (true) {
            val client = server.accept()

            launch(Dispatchers.IO) {
                clientCoroutine(client)
            }
        }
    }

    private suspend fun clientCoroutine(client: Socket) {
        val readChannel = client.openReadChannel()
        val writeChannel = client.openWriteChannel(autoFlush = true)
        val loginRes = login(client, readChannel, writeChannel)
        if (loginRes.first) {
            startCommunication(loginRes.second)
        } else {
            client.close()
        }
    }

    private suspend fun login(client: Socket, readChannel: ByteReadChannel, writeChannel: ByteWriteChannel): Pair<Boolean, String> {
        try {
            var name = ""
            var isSuccessfullyLogin = false
            while (true) {
                if (readChannel.availableForRead > 0) {
                    val fullMessage = getNewMessage(readChannel)
                    val message = fullMessage.first
                    name = message.data.senderName
                    isSuccessfullyLogin = clientList.addNewClient(client, name, readChannel, writeChannel)
                    if (isSuccessfullyLogin) {
                        val data = Data(0, name, "", "Client $name connected to chat", null)
                        val dataSize = data.getServerMessage().toByteArray().size
                        val header = Header(MessageType.SPECIAL, false, dataSize)
                        clientList.writeToEveryBody(Message(header, data), ByteArray(0))
                    }
                    break
                }
            }
            return Pair(isSuccessfullyLogin, name)
        } catch (e: Exception) {
            println("!E: Client connection was closed! He will come later probably?")
            e.printStackTrace()
            return Pair(false, "server")
        }
    }

    private suspend fun startCommunication(clientId: String) {
        try {
            println("Client $clientId connected to chat")
            var isWorking = true
            val receiver = clientList.getReadChannel(clientId) ?: throw NullPointerException()
            while (isWorking) {
                if (receiver.availableForRead > 0) {

                    val fullMessage = getNewMessage(receiver)
                    if (fullMessage.first.header.dataSize == 0)
                        break
                    val message = fullMessage.first
                    val fileByteArray = fullMessage.second

                    if (message.header.type == MessageType.MESSAGE) {
                        val fileSize = message.data.fileSize
                        val time = ZonedDateTime.now().toString().replace("[", "{").replace("]", "}")
                        val data = Data(
                            fileSize,
                            message.data.senderName,
                            time,
                            message.data.messageText,
                            message.data.fileName
                        )
                        val dataSize = data.getServerMessage().toByteArray().size
                        val resMessage = Message(
                            Header(
                                MessageType.MESSAGE,
                                message.header.isFileAttached,
                                dataSize
                            ),
                            data
                        )
                        clientList.writeToEveryBody(resMessage, fileByteArray)
                    } else if (message.data.messageText == "EXIT") {
                        clientList.finishConnection(message.data.senderName)
                        isWorking = false
                    } else {
                        println(
                            "Got message with type '${message.header.type}' and text " +
                                    "'${message.data.messageText}' from '${message.data.senderName}'"
                        )
                    }
                }
            }
        } catch (e: Exception) {
            println("!E: Connection with client was closed! Deleting him from clients list...")
            clientList.finishConnection(clientId)
        }
    }
}