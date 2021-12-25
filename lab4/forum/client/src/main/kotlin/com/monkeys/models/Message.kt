package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
data class Message (
    val time: String,
    val name: String,
    val msg: String
)