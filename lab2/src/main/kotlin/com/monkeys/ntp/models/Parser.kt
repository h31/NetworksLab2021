package com.monkeys.ntp.models

fun parseHostAndPort(arg: String) : Pair<String, Int> {
    return try {
        Pair(arg.split(":")[0], arg.split(":")[1].toInt())
    } catch (e: Exception) {
        Pair("127.0.0.1", 8081)
    }
}