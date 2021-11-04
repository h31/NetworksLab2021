package models

sealed class ResponseCode(val code: Short) {
    class NoError : ResponseCode(0)
    class FormatError: ResponseCode(1)
    class ServerFailure: ResponseCode(2)
    class NameError: ResponseCode(3)
    class Refused: ResponseCode(5)
    class NotImpl(type: Short): ResponseCode(type)

    companion object {
        fun of(code: Short): ResponseCode = when (code.toInt()) {
                0 -> NoError()
                1 -> FormatError()
                2 -> ServerFailure()
                3 -> NameError()
                5 -> Refused()
                else -> NotImpl(code)
        }
    }
}