package com.monkeys.terminal.models.response

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseAuthModel (
    override val status: String = "Error",
    override val message: AuthOkModel,
    @Contextual
    override val code: HttpStatusCode
) : ResponseModel
