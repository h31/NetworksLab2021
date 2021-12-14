package com.monkeys.terminal.models.response

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class OkResponseAuthModel (
    override val status: String,
    override val message: AuthOkModel,
    @Contextual
    override val code: HttpStatusCode
) : ResponseModel
