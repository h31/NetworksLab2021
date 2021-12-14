package com.monkeys.terminal.api

import com.monkeys.terminal.executeBashProcessWithResult
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.CdRequest
import com.monkeys.terminal.separateListByElements
import java.util.*

class UserController() {
    private val clients = Collections.synchronizedMap(
        mutableMapOf<String, String>()
    )

    fun addUser(model: AuthModel): String {
        val location = clients[model.login]
        return if (location != null) {
            location
        } else {
            clients[model.login] = "/home/"
            "/home/"
        }
    }

    fun ls(userName: String, request: String) : List<String>? {
        val location = clients[userName] + request.trim()
        val bashResult = executeBashProcessWithResult("ls $location")
        return separateListByElements(bashResult)
    }

    fun cd(request: CdRequest) : String {
        return ""
    }

    fun who() : List<String> {
        return emptyList()
    }

    fun kill() {
        println("kill")
    }

    fun logout(userName: String) {
        clients.remove(userName)
    }
}