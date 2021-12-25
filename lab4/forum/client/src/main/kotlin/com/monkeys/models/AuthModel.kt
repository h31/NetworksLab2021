package com.monkeys.models

import io.ktor.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class AuthModel(
    val login: String,
    val psw: String
) : Principal