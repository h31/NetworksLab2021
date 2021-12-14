package com.monkeys.terminal.models.response

import kotlinx.serialization.Serializable

@Serializable
data class OkListOfStrings(
    val msg: List<String>
) : OkModel