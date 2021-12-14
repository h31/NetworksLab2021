package com.monkeys.terminal.api

import com.monkeys.terminal.executeBashProcessWithResult
import com.monkeys.terminal.models.AuthModel
import com.monkeys.terminal.models.CdRequest
import com.monkeys.terminal.models.KillRequest
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

    fun ls(userName: String, request: String): List<String>? {
        val clientLocation = clients[userName]
        return if (clientLocation != null) {
            val location = clientLocation + request.trim()
            val bashResult = executeBashProcessWithResult("ls $location") ?: Collections.singletonList("")
            separateListByElements(bashResult)
        } else null
    }

    fun cd(userName: String, request: CdRequest): String? {
        val clientLocation = clients[userName]
        return if (clientLocation != null) {
            var res = executeBashProcessWithResult("cd ${request.location}")
            if (res == null) {
                res = executeBashProcessWithResult("cd $clientLocation${request.location}")
                if (res != null) {
                    clients[userName] = clientLocation + request.location + "/"
                    return clients[userName]!!
                } else {
                    "Error"
                }
            } else {
                clients[userName] = request.location + "/"
                return clients[userName]!!
            }
        } else {
            null
        }
    }

    fun who(): List<Pair<String, String>> {
        return clients.entries.map { it.key to it.value }
    }

    fun kill(killRequest: KillRequest) {
        clients.remove(killRequest.userToKill)
    }

    fun logout(userName: String) {
        clients.remove(userName)
    }
}