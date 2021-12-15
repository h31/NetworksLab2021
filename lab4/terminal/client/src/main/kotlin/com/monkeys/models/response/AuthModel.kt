package com.monkeys.terminal.models.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthModel(
    val msg: String,
    val jwt: String,
    val location: String
) : OkModel
