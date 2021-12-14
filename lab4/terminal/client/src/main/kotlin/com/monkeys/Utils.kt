package com.monkeys

import io.ktor.http.*

const val HOST = "0.0.0.0"
const val PORT = 8080
const val HTTP_PREF = "http://"
const val TOKEN_PREF = "Bearer "
const val SIGN_UP_URL = "/api/v1/auth/signup"
const val SIGN_IN_URL = "/api/v1/auth/signup"
const val LS_URL = "/api/v1/terminal/ls/"
const val CD_URL = "/api/v1/terminal/cd"
const val WHO_URL = "/api/v1/terminal/who"
const val KILL_URL = "/api/v1/terminal/kill"
const val LOGOUT_URL = "/api/v1/terminal/logout"
const val LS_WITHOUT_ARGS = ""
val OK_HTTP_STATUS_CODE = HttpStatusCode(200, "OK")

fun getURL(request: String) = "$HTTP_PREF$HOST:$PORT$request"
