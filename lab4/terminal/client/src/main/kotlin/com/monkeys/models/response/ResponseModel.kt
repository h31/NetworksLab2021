package com.monkeys.models.response

interface ResponseModel<T> {
    val status: String
    val message: T
}
