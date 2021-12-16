package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
data class OkAuth (
    val jwt: String
    )