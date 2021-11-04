package models

sealed class RecordClass(val code: Short) {
    class IN : RecordClass(1)
    class NotImpl(type: Short) : RecordClass(type)

    companion object {
        fun of(code: Short): RecordClass = when (code.toInt()) {
            1 -> IN()
            else -> NotImpl(code)
        }
    }
}