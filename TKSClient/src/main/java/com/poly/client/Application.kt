package com.poly.client

import com.poly.client.util.SERVER_HOST
import com.poly.client.util.SERVER_PORT
import java.util.*

class Application {
    fun startApplication() {
        println("Write your name:")
        val scanner = Scanner(System.`in`)
        MessageData.userName = scanner.nextLine()

        Thread {
            Client.startClient(SERVER_HOST, SERVER_PORT)
        }.start()

        Thread {
            Client.readMessage()
        }.start()

        while (true) {
            val message = MessageData.createMessage(scanner.nextLine())
            if (message != null) Buffer.senderBuffer.add(message)
        }
    }
}