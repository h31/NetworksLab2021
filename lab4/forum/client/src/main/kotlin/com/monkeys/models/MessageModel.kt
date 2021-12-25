package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
data class MessageModel(
    val subTheme: String,
    val msg: String
)
