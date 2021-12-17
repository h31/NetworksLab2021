package Models

import io.ktor.network.sockets.*

data class CustomSocket constructor (val aSocket: Socket) {
    val reader = aSocket.openReadChannel()
    val writer = aSocket.openWriteChannel(autoFlush = true)
}