package com.monkeys.terminal.models.response

data class OkListOfPairs(
    val response: List<Pair<String, String>>
) : OkModel