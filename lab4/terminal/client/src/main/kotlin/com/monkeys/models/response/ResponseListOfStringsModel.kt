package com.monkeys.terminal.models.response

import io.ktor.http.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class ResponseListOfStringsModel (
    override val status: String,
    override val message: ListOfStrings,
    @Contextual
    override val code: HttpStatusCode
) : ResponseModel