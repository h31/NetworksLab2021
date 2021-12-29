package com.monkeys

import io.ktor.http.*

const val HOST = "0.0.0.0"
const val PORT = 8080
const val TOKEN_PREF = "Bearer "
val SIGN_UP_URL = listOf("auth", "sign-up")
val SIGN_IN_URL = listOf("auth", "sign-in")
val HIERARCHY_REQUEST = listOf("forum", "request", "hierarchy")
val ACTIVE_USERS_REQUEST = listOf("forum", "request", "active-users")
val MESSAGE_REQUEST = listOf("forum", "request", "message")
val MESSAGE_LIST_REQUEST = listOf("forum", "request", "message-list/")
val LOGOUT_REQUEST = listOf("forum", "request", "logout")

fun getURL(path: List<String>) = URLBuilder(
    host = HOST,
    port = PORT,
).path(path).build()