package com.monkeys.terminal.models

import kotlinx.serialization.Serializable

@Serializable
enum class Roles {
    ADMIN,
    USER
}