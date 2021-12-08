package com.poly.client

import com.poly.client.util.SERVER_HOST
import com.poly.client.util.SERVER_PORT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Thread.currentThread
import java.util.*


class Application {
    fun startApplication() {
        println("Write your name:")
        val scanner = Scanner(System.`in`)
        MessageData.userName = scanner.nextLine()

        Thread {
            Client.startClient(SERVER_HOST, SERVER_PORT)
        }.start()

        while (!currentThread().isInterrupted) {
            Buffer.senderBuffer.add(MessageData.createMessage(scanner.nextLine()))
        }
    }
}