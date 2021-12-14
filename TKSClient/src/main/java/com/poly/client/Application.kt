package com.poly.client

import com.poly.client.util.SERVER_HOST
import com.poly.client.util.SERVER_PORT
import com.poly.sockets.MessageWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.lang.Thread.currentThread
import java.net.Socket
import java.util.*


class Application {
    fun startApplication() {
        println("Write your name:")
        val scanner = Scanner(System.`in`)
        MessageData.userName = scanner.nextLine()
        val socket = Socket(SERVER_HOST, SERVER_PORT)


        Thread {
            Client.startClient(socket)
        }.start()

        while (!currentThread().isInterrupted) {
            val sender = MessageWriter(socket.getOutputStream())
            sender.write(MessageData.createMessage(scanner.nextLine()))
        }
    }
}