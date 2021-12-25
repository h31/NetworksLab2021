package com.monkeys

import com.monkeys.models.MessageModel
import kotlinx.coroutines.isActive
import java.util.*

class Application {
    private val scanner = Scanner(System.`in`)
    private lateinit var forumService: ForumService


    suspend fun startClient() {
        println("Hello! What do you want to do?\n[1] - registration, [2] - authentication")
        val regOrAuth = scanner.nextLine()
        if (regOrAuth != "1" && regOrAuth != "2") {
            print("Invalid input. Try to connect again")
        } else {
            if (regOrAuth.toInt() == 1) {
                forumService = regOrLoginUser(
                    "Create a login: ",
                    "Create a password: "
                )
                if (forumService.reg() && forumService.auth())
                    startForum()
                else
                    println("Sorry! You were not registered")
            } else {
                forumService = regOrLoginUser(
                    "Enter login: ",
                    "Enter password: "
                )
                if (forumService.auth())
                    startForum()
                else
                    println("Incorrect username or password. Try to connect again")

            }
        }
    }

    suspend fun startForum() {
        println()
        println("You have successfully logged in and can watch forum:")
        val themes = forumService.hierarchy()
        if (themes != null) {
            printThemesBeauty(themes)
            println()
            printHelp()
            while (forumService.getClient().isActive) {
                val command = scanner.nextLine()
                val commandSplit = command.split(" ")
                when(commandSplit[0]) {
                    "ls" -> {getMessageList(command.replace(commandSplit[0], "").trim())}
                    "msg" -> {sendMessage(command.replace(commandSplit[0], "").trim())}
                    "who" -> {getListOfUsers()}
                    "themes" -> {watchThemesList()}
                    "logout" -> {logout()}
                }
            }

        }

    }

    private suspend fun getMessageList(subTheme: String) {
        val res = forumService.messageList(subTheme)
        if (res != null) {
            if (res.isEmpty()) {
                println("No messages in theme $subTheme...")
            } else {
                for (message in res) {
                    println("${message.name} [${message.time}]: ${message.msg}")
                }
            }
        }
    }

    private suspend fun sendMessage(subTheme: String) {
        println("Enter your message:")
        val msg = scanner.nextLine()
        val res = forumService.message(MessageModel(subTheme, msg))
        if (res) println("Success!")
    }

    private suspend fun watchThemesList() {
        val res = forumService.hierarchy()
        if (res != null) {
            printThemesBeauty(res)
        }
    }

    private suspend fun getListOfUsers() {
        val res = forumService.activeUsers()
        if (res != null) {
            for (user in res) {
                println(user)
            }
        }
    }

    private suspend fun logout() {
        val res = forumService.logout()
        if (res) {
            println("Good by!")
        }
    }

    private fun regOrLoginUser(loginStr: String, pswStr: String): ForumService {
        print(loginStr)
        val login = scanner.nextLine()
        print(pswStr)
        val psw = scanner.nextLine()
        return ForumService(login, psw)
    }

    private fun printThemesBeauty(themes: Map<String, List<String>>) {
        for (theme in themes) {
            println(theme.key)
            for(subTheme in theme.value) {
                println("\t|")
                println("\t|__$subTheme")
            }
        }
    }

    private fun printHelp() {
        println("""You can:
                | - ls {sub theme name} : watch list of messages 
                | - msg {sub theme name} : send message to sub theme
                | - who : watch all active users
                | - themes : watch all themes and sub-themes
                | - logout : logout
            """.trimMargin())
    }

}