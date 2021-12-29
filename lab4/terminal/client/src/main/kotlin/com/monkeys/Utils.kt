package com.monkeys

import io.ktor.http.*

const val HOST = "0.0.0.0"
const val PORT = 8080
const val TOKEN_PREF = "Bearer "
val SIGN_UP_URL = listOf("api", "v1", "auth", "signup")
val SIGN_IN_URL = listOf("api", "v1", "auth", "signin")
val LS_URL = listOf("api", "v1", "terminal", "ls")
val CD_URL = listOf("api", "v1", "terminal", "cd")
val WHO_URL = listOf("api", "v1", "terminal", "who")
val KILL_URL = listOf("api", "v1", "terminal", "kill")
val LOGOUT_URL = listOf("api", "v1", "terminal", "logout")

fun getURL(path: List<String>) = URLBuilder(
    host = HOST,
    port = PORT,
).path(path).build()

