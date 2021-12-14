package com.monkeys.terminal.models.response

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseModel (
    override val status: String = "Error",
    override val message: OkModel,
    @Contextual
    override val code: HttpStatusCode
) : ResponseModel
