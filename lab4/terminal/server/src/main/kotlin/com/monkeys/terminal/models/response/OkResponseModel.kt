package com.monkeys.terminal.models.response

import kotlinx.serialization.Serializable

@Serializable
data class OkResponseModel<T> (
    override val status: String,
    override val message: T,
) : ResponseModel<T>
