package models

import NOT_IMPL_MSG
import exceptions.NotImplTypeException

sealed class RecordType(val code: Short, private val str: String) {
    class A : RecordType(1, "A")
    class AAAA : RecordType(28, "AAAA")
    class NotImpl(type: Short) : RecordType(type, "NotImpl")
    data class MX(val size: Int): RecordType(15, "MX")
    data class TXT(val size: Int): RecordType(16, "TXT")

    companion object {
        fun of(code: Short): RecordType = when (code.toInt()) {
                1 -> A()
                28 -> AAAA()
                15 -> MX(0)
                16 -> TXT(0)
                else -> NotImpl(code)
        }
    }

    fun size(): Int = when(this) {
            is A -> 4
            is AAAA -> 16
            is MX -> this.size
            is TXT -> this.size
            is NotImpl -> throw NotImplTypeException(NOT_IMPL_MSG)
    }

    override fun toString(): String = str

}

