package com.monkeys.pcss.server

import com.monkeys.pcss.getNewMessage
import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.Data
import com.monkeys.pcss.models.message.Header
import com.monkeys.pcss.models.message.Message
import com.monkeys.pcss.models.message.MessageType
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.InetSocketAddress

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
        val loginRes = login(client)
        if (loginRes.first) {
            startCommunication(loginRes.second)
        } else {
            client.close()
        }
    }

    private suspend fun login(client: Socket): Pair<Boolean, String> {
        try {
            val receiver = client.openReadChannel()
            var name = ""
            var isSuccessfullyLogin: Boolean
            while (true) {
                if (receiver.availableForRead > 0) {
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

    private suspend fun startCommunication(clientId: String) {

    }
}