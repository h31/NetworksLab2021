package com.monkeys.terminal.api

import com.monkeys.terminal.getActiveUsers
import com.monkeys.terminal.killUser
import com.monkeys.terminal.models.KillRequest
import java.io.File

class UserController() {

    fun ls(baseLocation: String, locationToLs: String): List<String>? {
        val path = File(baseLocation, locationToLs)
        return if (path.isDirectory) {
            val res = getCorrectListOfFiles(path)
            res?.sorted()
        } else {
            val newPath = File(locationToLs)
            return if (newPath.isDirectory) {
                val res = getCorrectListOfFiles(newPath)
                res?.sorted()
            } else {
                null
            }
        }
    }

    fun cd(baseLocation: String, locationToCd: String): String? {
        val path = File(baseLocation, locationToCd)
        return if (path.isDirectory) {
            path.absolutePath
        } else {
            val newPath = File(locationToCd)
            return if (newPath.isDirectory) {
                newPath.absolutePath
            } else {
                null
            }
        }
    }

    fun who(): List<String> = getActiveUsers()


    fun kill(killRequest: KillRequest) {
        killUser(killRequest.userToKill)
    }

    fun logout(userName: String) {
        killUser(userName)
    }

    private fun getCorrectListOfFiles(dir : File) : List<String>? {
        val list = dir.listFiles()
        val res = mutableListOf<String>()
        if (list != null) {
            res.addAll(list.toList().map {
                if (it.isFile) {
                    "[F]" + it.name
                } else {
                    "[D]" + it.name

                }
            })
        } else return null
        return res
    }
}