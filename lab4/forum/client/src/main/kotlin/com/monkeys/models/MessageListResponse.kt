package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
data class MessageListResponse(
    val messages: List<Message>
)