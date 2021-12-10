package com.monkeys.pcss.models

import com.monkeys.pcss.DOWNLOADS_DIR
import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import com.monkeys.pcss.models.message.MessageType
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.io.File
import java.net.SocketException
import java.nio.ByteBuffer
import java.util.*

class ClientList() {
    private val clients = Collections.synchronizedMap(
        mutableMapOf<String, ClientChannels>()
    )
    private val socketList = Collections.synchronizedMap(mutableMapOf<String, Socket>())
    private val logger = LoggerFactory.getLogger("com.monkeys.pcss.models.ClientListKt")

    suspend fun addNewClient(socket: Socket, newId: String, readChannel: ByteReadChannel, writeChannel: ByteWriteChannel): Boolean {
        return if (clients.keys.contains(newId) || newId == "server") {
            val data = Data(
                senderName = "server", messageText =
                "Name is taken, please try to connect again"
            )
            createMessageAndSend(newId, data)
            false
        } else {
            val downloadDir = File(DOWNLOADS_DIR)
            if (!downloadDir.exists())
                downloadDir.mkdir()
            clients[newId] = ClientChannels(readChannel, writeChannel)
            socketList[newId] = socket
            val data = Data(
                senderName = "server", messageText =
                "Great, your name now is $newId, you can communicate. There are ${clients.size - 1} people in the chat excepts you."
            )
            createMessageAndSend(newId, data)
            true
        }
    }

    private suspend fun createMessageAndSend(clientId: String, data: Data) {
        val dataSize = data.getServerMessage().toByteArray().size
        val header = Header(MessageType.LOGIN, false, dataSize)
        val message = ByteBuffer.wrap(Message(header, data).getMessage())
        val writeChannel = getWriteChannel(clientId)
        writeChannel!!.writeFully(message)
    }

    suspend fun getReadChannel(clientId: String) = clients[clientId]?.readChannel
    suspend fun getWriteChannel(clientId: String) = clients[clientId]?.writeChannel

    suspend fun finishConnection(id: String) {
        clients[id]!!.writeChannel.close()
        clients.remove(id)
        closeSocketSuspending(socketList[id]!!)
        socketList.remove(id)
        val data = Data(0, id, "", "Client $id disconnected from chat", null)
        val dataSize = data.getServerMessage().toByteArray().size
        val header = Header(MessageType.SPECIAL, false, dataSize)
        writeToEveryBody(Message(header, data), ByteArray(0))
    }

    suspend fun closeSocketSuspending(socket: Socket) {
        withContext(Dispatchers.IO) {
            socket.close()
        }
    }

    suspend fun writeToEveryBody(message: Message, fileByteArray: ByteArray) {
        val name = message.data.senderName
        val names = mutableListOf<String>()
        clients.forEach { client ->
            try {
                if (client.key != name) {
                    val sender = client.value.writeChannel
                    val messageBuffer = message.getMessage() + fileByteArray
                    sender.writeFully(messageBuffer)
                    names.add(client.key)
                }
            } catch (e: SocketException) {
                logger.error("!E: Connection with client ${client.key} was closed!")
                names.remove(client.key)
                finishConnection(client.key)
            } catch (e: Exception) {
                logger.error("!E: error with client ${client.key}!")
                logger.error(e.stackTraceToString())
                names.remove(client.key)
                finishConnection(client.key)
            }
        }
        logger.info("Message '${String(message.getMessage())}' was send to this clients: $names")
    }
}