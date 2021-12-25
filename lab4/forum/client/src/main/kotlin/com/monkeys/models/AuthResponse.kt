package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val jwt: String
)