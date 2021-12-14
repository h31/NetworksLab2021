package com.monkeys.terminal.models.response

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseListOfStringModel (
    override val status: String,
    override val message: OkListOfStrings,
    @Contextual
    override val code: HttpStatusCode
) : ResponseModel