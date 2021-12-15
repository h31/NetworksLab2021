package com.monkeys.terminal.models.response

import kotlinx.serialization.Serializable

@Serializable
data class ListOfStrings(
    val msg: String,
    val response: List<String>
) : OkModel