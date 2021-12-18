package com.monkeys.terminal.models.response

interface ResponseModel<T> {
    val status: String
    val message: T
}
