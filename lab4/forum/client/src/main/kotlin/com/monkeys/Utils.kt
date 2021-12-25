package com.monkeys

const val HOST = "0.0.0.0"
const val PORT = 8080
const val HTTP_PREF = "http://"
const val TOKEN_PREF = "Bearer "
const val SIGN_UP_URL = "/auth/sign-up"
const val SIGN_IN_URL = "/auth/sign-in"
const val HIERARCHY_REQUEST = "/request/hierarchy"
const val ACTIVE_USERS_REQUEST = "/request/active-users"
const val MESSAGE_REQUEST = "/request/message"
const val MESSAGE_LIST_REQUEST = "/request/message-list/"
const val LOGOUT_REQUEST = "/request/logout"

fun getURL(request: String) = "$HTTP_PREF$HOST:$PORT$request"