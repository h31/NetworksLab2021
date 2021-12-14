package com.monkeys.terminal.models.response

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseStringModel (
    override val status: String,
    override val message: OkString,
    @Contextual
    override val code: HttpStatusCode
) : ResponseModel