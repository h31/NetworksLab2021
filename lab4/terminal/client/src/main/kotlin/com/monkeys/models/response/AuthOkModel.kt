package com.monkeys.models.response

import kotlinx.serialization.Serializable

@Serializable
data class AuthOkModel(
    val jwt: String,
    val location: String
) : OkModel
