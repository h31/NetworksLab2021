package com.monkeys.pcss.server

import com.monkeys.pcss.STANDARD_PORT
import com.monkeys.pcss.models.ClientList
import com.monkeys.pcss.models.message.Data
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.ServerSocket
import java.net.SocketException

class Server {

    private val clientList = ClientList()

    fun start() = runBlocking {
        try {
            ServerSocket(STANDARD_PORT).use { server ->
                println("Server is running")
                while (true) {
                    val client = server.accept()
                    if (client.isConnected) {
                        launch(Dispatchers.Default) {
                            clientCoroutine(client, clientList)
                        }
                    }
                }
            }

        } catch (e: SocketException) {
            println("Error: server was not closed or there was another problem")
            e.printStackTrace()
        }
    }

}