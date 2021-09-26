package com.monkeys.pcss.server

import com.monkeys.pcss.generateMessageId
import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.*
import kotlinx.coroutines.delay
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.time.ZoneId


suspend fun clientCoroutine(client: Socket, clientList: ClientList) {
    val loginRes = login(client, clientList)
    if (loginRes.first) {
        startCommunication(loginRes.second, clientList)
    } else {
        delay(5000)
        client.close()
    }
}

fun login(client: Socket, clientList: ClientList): Pair<Boolean, String> {
    val receiver = BufferedReader(InputStreamReader(client.getInputStream()))
    var name = ""
    var isSuccessfullyLogin = false
    while (true) {
        val message = receiver.readLine()
        val parsedMessage = parseMessage(message)
        name = parsedMessage.data.senderName
        val zoneId = ZoneId.of(parsedMessage.data.messageText)
        isSuccessfullyLogin = clientList.addNewClient(client, name, zoneId)
        break
    }
    return Pair(isSuccessfullyLogin, name)
}

fun startCommunication(clientId: String, clientList: ClientList) {
    println("Client $clientId connected to chat")
    var isWorking = true
    val receiver = BufferedReader(InputStreamReader(clientList.getInputStream(clientId)))
    while (isWorking) {
        val message = receiver.readLine()
        val parsedMessage = parseMessage(message)
        if (parsedMessage.header.type == MessageType.MESSAGE) {
            println("Got new message from ${parsedMessage.data.senderName}")
            val messageId = generateMessageId()
            val file = parsedMessage.file

            val data = Data(
                messageId,
                parsedMessage.data.senderName,
                "04:20",
                parsedMessage.data.messageText,
                parsedMessage.data.fileName
            )
            val resMessage = Message(
                Header(
                    MessageType.MESSAGE,
                    parsedMessage.header.isFileAttached,
                    data.getServerMessage().toByteArray().size
                ),
                data,
                file
            )
            clientList.writeToEveryBody(resMessage)
        } else if (parsedMessage.data.messageText == "EXIT") {
            clientList.finishConnection(parsedMessage.data.senderName)
            println("Got closing message from client with name ${parsedMessage.data.senderName}")
            isWorking = false
        } else {
            println("Got message with type '${parsedMessage.header.type}' and text '${parsedMessage.data.messageText}' from '${parsedMessage.data.senderName}'")
        }
    }
}