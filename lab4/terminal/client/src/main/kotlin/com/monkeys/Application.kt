package com.monkeys

import com.monkeys.service.TerminalService
import kotlinx.coroutines.isActive
import java.util.*

class Application {

    private val scanner = Scanner(System.`in`)
    private lateinit var terminalService: TerminalService

    suspend fun startClient() {
        println("Hello! What do you want to do?\n[1] - registration, [2] - authentication")
        val regOrAuth = scanner.nextLine()
        if (regOrAuth != "1" && regOrAuth != "2") {
            print("Invalid input. Try to connect again")
        } else {
            if (regOrAuth.toInt() == 1) {
                terminalService = regOrLoginUser("Create a login: ",
                    "Create a password: ")
                if (terminalService.role == "Error") {
                    print("Invalid input. Try to connect again")
                } else {
                    if (terminalService.reg() && terminalService.auth())
                        startTerminal()
                    else
                        println("Sorry! You were not registered")
                }
            } else {
                terminalService = regOrLoginUser("Enter login: ",
                    "Enter password: ")
                if (terminalService.role == "Error") {
                    print("Invalid input. Try to connect again")
                } else {
                    if (terminalService.auth())
                        startTerminal()
                    else
                        println("Incorrect username or password. Try to connect again")
                }
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
                if (ls.first) {
                    ls.second.forEach {
                        println(it)
                    }
                } else {
                    println("Bad credentials")
                }
            }
            2 -> {
                if (command[1].contains("/"))
                    println("You can use the command ls no further than 1 folder")
                else {
                    val ls = terminalService.getDirContent(command[1])
                    if (ls.first) {
                        ls.second.forEach {
                            println(it)
                        }
                    } else {
                        println("Bad credentials")
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
            1 -> println(terminalService.getCurrentDir()+"> ")
            2 -> {
                val msg = terminalService.getChangeDir(command[1])
                if (msg == "Wrong location to cd")
                    println(msg)
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
            if (ls.first) {
                val list = ls.second
                list.forEach {
                    if (it.first.length > biggestLength)
                        biggestLength = it.first.length
                }
                biggestLength += 3
                list.forEach {
                    println(it.first.padEnd(biggestLength, ' ') + it.second)
                }
            } else
                println("Bad credentials")
        } else {
            println("Wrong command")
        }
    }

    private suspend fun callKill(command: List<String>) {
        if (command.size == 2) {
            when (val msg = terminalService.kill(command[1])) {
                "${command[1]} was killed" -> {
                    if (command[1] == terminalService.getLogin()) {
                        println(msg)
                        terminalService.stopClient()
                    }
                    else
                        println(msg)
                }
                "You have not enough rights" -> {
                    println(msg)
                }
                else ->
                    println("Unsuccessful logout")
            }
        } else
            println("Wrong command")
    }

    private suspend fun callLogout(command: List<String>) {
        if (command.size == 1) {
            val msg = terminalService.logout()
            if (msg == "You was killed (logout successful)") {
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
        print("Choose your role: ")
        println("[1] - user, [2] - admin")
        var role = scanner.nextLine()
        role = if (role != "1" && role != "2") {
            "Error"
        } else {
            if (role.toInt() == 1)
                "user"
            else
                "admin"
        }
        return TerminalService(login, psw, role)
    }
}
