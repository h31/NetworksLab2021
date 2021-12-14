package com.monkeys

import com.monkeys.service.TerminalService
import kotlinx.coroutines.isActive
import java.util.*

class Application {

    private val scanner = Scanner(System.`in`)
    private lateinit var terminalService: TerminalService

    suspend fun startClient() {
        print("Enter login: ")
        val login = scanner.nextLine()
        print("Enter password: ")
        val psw = scanner.nextLine()
        terminalService = TerminalService(login, psw)
        if (terminalService.auth())
            startTerminal()
        else
            println("Incorrect username or password. Try to connect again")
    }

    private suspend fun startTerminal() {
        println()
        println("You have successfully logged in and can enter commands:")
        println("ls, cd, who, kill, logout")
        while (terminalService.getClient().isActive) {
            val command = scanner.nextLine().split(" ")
            when (command[0]) {
                "ls" -> callLs(command)
                "cd" -> callCd(command)
                "who" -> callWho(command)
                "kill" -> callKill(command)
                "logout" -> callLogout(command)
                else -> println("You entered the wrong command")
            }
        }
    }

    private suspend fun callLs(command: List<String>) {
        when (command.size) {
            1 -> {
                val ls = terminalService.getDirContent(LS_WITHOUT_ARGS)
                ls.forEach {
                    println(it)
                }
            }
            2 -> {
                if (command[1].contains("/"))
                    println("")
                else {
                    val ls = terminalService.getDirContent(command[1])
                    ls.forEach {
                        println(it)
                    }
                }
            }
            else -> {
                println("Wrong command")
            }
        }
    }

    private suspend fun callCd(command: List<String>) {
        when (command.size) {
            1 -> println(terminalService.getCurrentDir())
            2 -> {
                print(terminalService.getChangeDir())
            }
            else -> {
                println("Wrong command")
            }
        }
    }

    private suspend fun callWho(command: List<String>) {
        if (command.size == 1) {
            val ls = terminalService.getCurrUsersAndDirs()
            var biggestLength = 0
            ls.forEach {
                if (it.first.length > biggestLength)
                    biggestLength = it.first.length
            }
            biggestLength += 3
            ls.forEach {
                println(it.first.padEnd(biggestLength, ' ') + it.second)
            }
        } else {
            println("Wrong command")
        }
    }

    private fun callKill(command: List<String>) {
        if (command.size == 2)
            println("")
        else
            println("Wrong command")
    }

    private fun callLogout(command: List<String>) {
        if (command.size == 1) {
            println("Bye!")
        } else {
            println("Wrong command")
        }
    }
}
