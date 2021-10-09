package com.monkeys.pcss.server

import com.monkeys.pcss.getNewMessage
import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.*
import java.io.BufferedInputStream
import java.net.Socket
import java.time.ZonedDateTime


suspend fun clientCoroutine(client: Socket, clientList: ClientList) {
    val loginRes = login(client, clientList)
    if (loginRes.first) {
        startCommunication(loginRes.second, clientList)
    } else {
        client.close()
    }
}

fun login(client: Socket, clientList: ClientList): Pair<Boolean, String> {
    try {
        val receiver = BufferedInputStream(client.getInputStream())
        var name = ""
        var isSuccessfullyLogin = false
        while (true) {
            if (receiver.available() > 0) {
                val fullMessage = getNewMessage(receiver)
                val message = fullMessage.first
                name = message.data.senderName
                isSuccessfullyLogin = clientList.addNewClient(client, name)
                if (isSuccessfullyLogin) {
                    val data = Data(0, name, "", "Client $name connected to chat", null)
                    val dataSize = data.getServerMessage().length
                    val header = Header(MessageType.SPECIAL, false, dataSize)
                    clientList.writeToEveryBody(Message(header, data), ByteArray(0))
                }
                break
            }
        }
        return Pair(isSuccessfullyLogin, name)
    } catch (e: Exception) {
        println("!E: Client connection was closed! He will come later probably?")
        return Pair(false, "server")
    }
}

fun startCommunication(clientId: String, clientList: ClientList) {
    try {
        println("Client $clientId connected to chat")
        var isWorking = true
        val receiver = clientList.getInputStream(clientId)
        while (isWorking) {
            if (receiver.available() > 0) {

                val fullMessage = getNewMessage(receiver)
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
                    val dataSize = data.getServerMessage().length
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
