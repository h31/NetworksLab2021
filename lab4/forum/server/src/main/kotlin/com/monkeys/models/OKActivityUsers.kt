package com.monkeys.models

import kotlinx.serialization.Serializable

@Serializable
class OKActivityUsers (
    val users: List<String>
)