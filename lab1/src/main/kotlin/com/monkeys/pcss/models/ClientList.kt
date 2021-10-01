package com.monkeys.pcss.models

import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import com.monkeys.pcss.models.message.MessageType
import com.monkeys.pcss.send
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.util.*

class ClientList() {
    private val clients = Collections.synchronizedMap(
        mutableMapOf<String,
                Pair<BufferedInputStream, BufferedOutputStream>>()
    )
    private val socketList = Collections.synchronizedMap(mutableMapOf<String, Socket>())

    fun addNewClient(socket: Socket, newId: String): Boolean {
        return if (clients.keys.contains(newId) || newId == "server") {
            val data = Data(
                senderName = "server", messageText =
                "Name is taken, please try to connect again"
            )
            val header = Header(MessageType.LOGIN, false, 0)
            socket.getOutputStream().write(Message(header, data).getMessage())
            false
        } else {
            clients[newId] = Pair(
                BufferedInputStream(socket.getInputStream()),
                BufferedOutputStream(socket.getOutputStream())
            )
            socketList[newId] = socket
            val data = Data(
                senderName = "server", messageText =
                "Great, your name now is $newId, you can communicate"
            )
            val header = Header(MessageType.LOGIN, false, 0)
            socket.getOutputStream().write(Message(header, data).getMessage())
            true
        }
    }

    fun finishConnection(id: String) {
        clients.remove(id)
        socketList[id]!!.close()
        socketList.remove(id)
    }

    fun getInputStream(id: String): InputStream {
        return clients[id]!!.first
    }

    fun getOutputStream(id: String): OutputStream {
        return clients[id]!!.second
    }

    fun getSocket(id: String): Socket {
        return socketList[id]!!
    }

    fun writeToEveryBody(message: Message, fileByteArray: ByteArray) {
        val name = message.data.senderName
        val names = mutableListOf<String>()
        clients.forEach { client ->
            try {
                if (client.key != name) {
                    val sender = client.value.second
                    send(sender, message.getMessage())
                    if (fileByteArray.isNotEmpty()) {
                        send(sender, fileByteArray)
                    }
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