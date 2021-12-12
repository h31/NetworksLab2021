package com.monkeys

const val HOST = "localhost"
const val PORT = 8081
const val HTTP_PREF = "http://"

fun getURL(request: String) = "$HTTP_PREF$HOST:$PORT$request"
