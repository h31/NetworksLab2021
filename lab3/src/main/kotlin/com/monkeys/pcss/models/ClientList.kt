package com.monkeys.pcss.models

import com.monkeys.pcss.DOWNLOADS_DIR
import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import com.monkeys.pcss.models.message.MessageType
import io.ktor.network.sockets.*
import io.ktor.utils.io.*
import java.io.File
import java.nio.ByteBuffer
import java.util.*

class ClientList() {
    private val clients = Collections.synchronizedMap(
        mutableMapOf<String, ClientChannels>()
    )
    private val socketList = Collections.synchronizedMap(mutableMapOf<String, Socket>())

    suspend fun addNewClient(socket: Socket, newId: String, readChannel: ByteReadChannel, writeChannel: ByteWriteChannel): Boolean {
        return if (clients.keys.contains(newId) || newId == "server") {
            val data = Data(
                senderName = "server", messageText =
                "Name is taken, please try to connect again"
            )
            val dataSize = data.getServerMessage().length
            val header = Header(MessageType.LOGIN, false, dataSize)
            val message = ByteBuffer.wrap(Message(header, data).getMessage())
            writeChannel.writeFully(message)
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
            val dataSize = data.getServerMessage().length
            val header = Header(MessageType.LOGIN, false, dataSize)
            val message = ByteBuffer.wrap(Message(header, data).getMessage())
            writeChannel.writeFully(message)
            true
        }
    }

    suspend fun getReadChannel(clientId: String) = clients[clientId]?.readChannel

    suspend fun finishConnection(id: String) {
        clients.remove(id)
        socketList[id]!!.close()
        socketList.remove(id)
        val data = Data(0, id, "", "Client $id disconnected from chat", null)
        val dataSize = data.getServerMessage().length
        val header = Header(MessageType.SPECIAL, false, dataSize)
        writeToEveryBody(Message(header, data), ByteArray(0))
    }

    suspend fun writeToEveryBody(message: Message, fileByteArray: ByteArray) {
        val name = message.data.senderName
        val names = mutableListOf<String>()
        clients.forEach { client ->
            try {
                if (client.key != name) {
                    val sender = client.value.writeChannel
                    val messageBuffer = message.getMessage().plus(fileByteArray)
                    sender.writeFully(messageBuffer)
                    names.add(client.key)
                }
            } catch (e: Exception) {
                println("!E: Connection with client ${client.key} was closed!")
                names.remove(client.key)
                finishConnection(client.key)
            }
        }
        println("Message '${String(message.getMessage())}' was send to this clients: $names")
    }
}