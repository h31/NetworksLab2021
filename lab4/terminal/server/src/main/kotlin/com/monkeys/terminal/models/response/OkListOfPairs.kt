package com.monkeys.terminal.models.response

data class OkListOfPairs(
    val msg: List<Pair<String, String>>
) : OkModel