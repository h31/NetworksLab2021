package models

sealed class OperationCode(val code: Short) {
    class Query : OperationCode(0)
    class NotImpl(code: Short): OperationCode(code)

    companion object {
        fun of(code: Short): OperationCode = when (code.toInt()) {
                0 -> Query()
                else -> NotImpl(code)
        }
    }
}