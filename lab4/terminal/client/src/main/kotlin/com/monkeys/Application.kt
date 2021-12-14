package com.monkeys

import com.monkeys.service.TerminalService
import kotlinx.coroutines.isActive
import java.util.*

class Application {

    private val scanner = Scanner(System.`in`)
    private lateinit var terminalService: TerminalService

    suspend fun startClient() {
        println("Hello what do you want to do?\n[1] - registration, [2] - authentication")
        val regOrAuth = scanner.nextLine().toInt()
        if (regOrAuth != 1 && regOrAuth != 2) {
            print("Invalid input. Try to connect again")
        } else {
            if (regOrAuth == 1) {
                terminalService = regOrLoginUser("Create a login: ", "Create a password: ")
                if (terminalService.reg())
                    startTerminal()
                else
                    println("Sorry! You were not registered")
            } else {
                terminalService = regOrLoginUser("Enter login: ", "Enter password: ")
                if (terminalService.auth())
                    startTerminal()
                else
                    println("Incorrect username or password. Try to connect again")
            }
        }
    }

    private suspend fun startTerminal() {
        println()
        println("You have successfully logged in and can enter commands:")
        println("ls, cd, who, kill, logout")
        println()
        while (terminalService.getClient().isActive) {
            print("${terminalService.getCurrentDir()}> ")
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
            1 -> print(terminalService.getCurrentDir()+"> ")
            2 -> {
                print(terminalService.getChangeDir(command[1]))
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

    private suspend fun callKill(command: List<String>) {
        if (command.size == 2) {
            val msg = terminalService.kill()
            if (msg == "OK") {
                println(msg)
                terminalService.stopClient()
            } else {
                println("Unsuccessful logout")
            }
        } else
            println("Wrong command")
    }

    private suspend fun callLogout(command: List<String>) {
        if (command.size == 1) {
            val msg = terminalService.logout()
            if (msg == "OK") {
                println(msg)
                terminalService.stopClient()
            } else {
                println("Unsuccessful logout")
            }
        } else {
            println("Wrong command")
        }
    }

    private fun regOrLoginUser(login_str: String, psw_str: String): TerminalService {
        print(login_str)
        val login = scanner.nextLine()
        print(psw_str)
        val psw = scanner.nextLine()
        return TerminalService(login, psw)
    }
}
