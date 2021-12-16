package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
class OkListOfMessage (
    val messages: List<Message>
)