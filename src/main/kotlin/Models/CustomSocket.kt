package Models

import io.ktor.network.sockets.*

data class CustomSocket constructor (val aSocket: Socket) {
    var reader = aSocket.openReadChannel()
    var writer = aSocket.openWriteChannel(autoFlush = true)
}