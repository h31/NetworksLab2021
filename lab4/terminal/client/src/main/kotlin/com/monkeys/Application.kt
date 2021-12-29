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
            return
        }
        if (regOrAuth.toInt() == 1) {
            terminalService = regOrLoginUser(
                "Create a login: ",
                "Create a password: "
            )
            if (terminalService.reg() && terminalService.auth())
                startTerminal()
            else
                println("Sorry! You were not registered")
        } else {
            terminalService = regOrLoginUser(
                "Enter login: ",
                "Enter password: "
            )
            if (terminalService.auth())
                startTerminal()
            else
                println("Incorrect username or password. Try to connect again")

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
                else -> println("Wrong command")
            }
        }
    }

    private suspend fun callLs(command: List<String>) {
        if (command.size != 1 && command.size != 2) {
            println("Wrong command")
            return
        }
        var dir = ""
        if (command.size == 2)
            dir = command[1]
        try {
            val ls = terminalService.getDirContent(dir)
            ls.forEach {
                println(it)
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }


    private suspend fun callCd(command: List<String>) {
        when (command.size) {
            1 -> return
            2 -> {
                try {
                    terminalService.getChangeDir(command[1])
                } catch (e: Exception) {
                    println(e.message)
                }
            }
            else -> {
                println("Wrong command")
            }
        }
    }

    private suspend fun callWho(command: List<String>) {
        if (command.size == 1) {
            try {
                val who = terminalService.getCurrUsersAndDirs()
                who.forEach {
                    println(it)
                }
            } catch (e: Exception) {
                println(e.message)
            }
        } else {
            println("Wrong command")
        }
    }

    private suspend fun callKill(command: List<String>) {
        if (command.size == 2) {
            try {
                println(terminalService.kill(command[1]))
            } catch (e: Exception) {
                println(e.message)
            }
        } else
            println("Wrong command")
    }

    private suspend fun callLogout(command: List<String>) {
        if (command.size == 1) {
            val msg = terminalService.logout()
            println(msg)
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