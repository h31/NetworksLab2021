package com.monkeys.terminal.models.response

import kotlinx.serialization.Serializable

@Serializable
data class ListOfPairs(
    val msg: String,
    val response: List<Pair<String, String>>
) : OkModel