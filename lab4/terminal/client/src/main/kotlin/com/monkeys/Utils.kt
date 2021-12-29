package com.monkeys

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.http.cio.*
import io.ktor.util.*
import io.ktor.util.url

const val HOST = "0.0.0.0"
const val PORT = 8080
const val HTTP_PREF = "http://"
const val TOKEN_PREF = "Bearer "
const val SIGN_UP_URL = "/api/v1/auth/signup"
const val SIGN_IN_URL = "/api/v1/auth/signin"
const val LS_URL = "/api/v1/terminal/ls"
const val CD_URL = "/api/v1/terminal/cd"
const val WHO_URL = "/api/v1/terminal/who"
const val KILL_URL = "/api/v1/terminal/kill"
const val LOGOUT_URL = "/api/v1/terminal/logout"

fun getURL(request: String) = "$HTTP_PREF$HOST:$PORT$request"

fun url(path: String): String =
    url {
        protocol = URLProtocol.HTTPS
        host = HOST
        path(path)
    }

