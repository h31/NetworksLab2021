package com.monkeys.terminal.models.response

import kotlinx.serialization.Serializable

@Serializable
data class OkString(
    val msg: String,
    val response: String
) : OkModel