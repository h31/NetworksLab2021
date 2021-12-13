package com.monkeys.terminal.models

import io.ktor.auth.*
import kotlinx.serialization.Serializable

@Serializable
data class AuthModel(
    val login: String,
    val password: String,
    val role: String
) : Principal