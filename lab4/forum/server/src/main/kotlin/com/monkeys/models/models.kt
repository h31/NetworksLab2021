package com.monkeys.models

import io.ktor.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class AuthModel(
    val login: String,
    val psw: String
) : Principal

@Serializable
data class Message (
    val time: String,
    val name: String,
    val msg: String
)

@Serializable
data class MessageModel(
    val subTheme: String,
    val msg: String
)

@Serializable
class OKActivityUsers (
    val users: List<String>
)

@Serializable
data class OkAuth (
    val jwt: String
)

@Serializable
class OkHierarchy (
    //list with a pair: main_theme and list of sub_themes
    val hierarchy: Map<String, List<String>>
)

@Serializable
class OkListOfMessage (
    val messages: List<Message>
)