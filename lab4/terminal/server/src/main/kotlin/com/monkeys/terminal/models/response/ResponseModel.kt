package com.monkeys.terminal.models.response

import io.ktor.http.*

interface ResponseModel {
    val status: String
    val message: OkModel
    val code: HttpStatusCode
}
