package com.monkeys

import java.util.*

class Application {

    private val scanner = Scanner(System.`in`)

    fun startClient() {
        print("Enter login: ")
        val login = scanner.nextLine()
        print("Enter password: ")
        val psw = scanner.nextLine()

        //if ()
        //else
        startTerminal()
        println(getURL("/dfv"))
    }

    private fun startTerminal() {
        println()
        println("You have successfully logged in and can enter commands:")
        println("ls, cd, who, kill, logout")
        when (scanner.nextLine().split(" ")[0]) {
            "ls" -> callLs()
            "cd" -> println()
            "who" -> println()
            "kill" -> println()
            "logout" -> println()
            else -> println("You entered the wrong command")
        }
    }

    private fun callLs() {

    }
}
