package com.monkeys.models.response

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponseModel<T> (
    override val status: String = "Error",
    override val message: T,
) : ResponseModel<T>
