package com.monkeys.terminal

import java.io.BufferedReader
import java.io.InputStreamReader

fun executeBashProcessWithResult(command: String): List<String>? {
    val processBuilder = ProcessBuilder()
    processBuilder.command("bash", "-c", command)
    val process = processBuilder.start()
    process.waitFor()
    return if (process.exitValue() == 0) {
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        reader.readLines()
    } else {
        return null
    }
}

fun separateListByElements(list: List<String>) : List<String>? {
    return if (list.isEmpty()) {
        null
    } else {
        val res = mutableListOf<String>()
        list.forEach{
            res.addAll(it.split(" "))
        }
        res
    }
}