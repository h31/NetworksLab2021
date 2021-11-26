package com.monkeys.pcss.models

enum class MessageState {
    //states of Header/body/file read
    LOGIN,
    HEADER_READ,
    BODY_READ,
    DONE
}