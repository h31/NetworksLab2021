package com.monkeys.terminal.api

import com.monkeys.terminal.getActiveUsers
import com.monkeys.terminal.killUser
import com.monkeys.terminal.models.KillRequest
import java.io.File

class UserController() {

    fun ls(baseLocation: String, locationToLs: String): Map<String, Boolean>? {
        val path = resolvePath(baseLocation, locationToLs)
        return if (path != null) {
            val res = getCorrectListOfFiles(path)
            res?.toSortedMap()
        } else {
            null
        }
    }

    fun cd(baseLocation: String, locationToCd: String): String? {
        val path = resolvePath(baseLocation, locationToCd)
        return path?.canonicalPath
    }

    fun who(): List<String> = getActiveUsers()


    fun kill(killRequest: KillRequest) {
        killUser(killRequest.userToKill)
    }

    fun logout(userName: String) {
        killUser(userName)
    }

    private fun getCorrectListOfFiles(dir : File) : Map<String, Boolean>? {
        val list = dir.listFiles()
        return list?.toList()?.associateBy({it.name}, {it.isDirectory})
    }

    private fun resolvePath(base: String, changed: String): File? {
        var path = File(changed)
        if (path.isAbsolute) {
            if (!path.isDirectory) {
                return null
            }
        } else {
            path = File(base, changed)
            if (!path.isDirectory) {
                return null
            }
        }
        return path
    }
}