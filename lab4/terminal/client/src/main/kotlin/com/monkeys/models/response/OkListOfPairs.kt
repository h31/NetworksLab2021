package com.monkeys.terminal.models.response

import kotlinx.serialization.Serializable

@Serializable
data class OkListOfPairs(
    val msg: List<Pair<String, String>>
) : OkModel