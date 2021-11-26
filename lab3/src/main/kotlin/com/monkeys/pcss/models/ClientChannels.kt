package com.monkeys.pcss.models

import io.ktor.utils.io.*

data class ClientChannels(
    val readChannel: ByteReadChannel,
    val writeChannel: ByteWriteChannel
) {
}