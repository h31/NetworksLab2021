package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
data class ActivityUsersResponse (
    val users: List<String>
)
