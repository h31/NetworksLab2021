package com.monkeys.terminal.models.response

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseModel<T> (
    override val status: String = "Error",
    override val message: T,
) : ResponseModel<T>
